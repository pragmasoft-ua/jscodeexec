package api.demo.graalvmdemo.service;

import api.demo.graalvmdemo.model.Script;
import api.demo.graalvmdemo.model.ScriptStatus;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ScriptService {
    private static final AtomicLong ID_COUNTER = new AtomicLong();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    private final ConcurrentHashMap<Long, Script> scriptsStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Future<?>> runningScripts = new ConcurrentHashMap<>();


    public Script submitScript(String scriptBody, boolean isNonBlocking, LocalDateTime scheduledTime) {
        final Script script = new Script(scriptBody);
        script.setId(ID_COUNTER.incrementAndGet());
        scriptsStorage.put(script.getId(), script);

        Runnable task = () -> executeScript(script);

        Future<?> future;
        if (scheduledTime != null) {
            long delay = LocalDateTime.now().until(scheduledTime, ChronoUnit.MILLIS);
            script.setStatus(ScriptStatus.SCHEDULED);
            script.setScheduledTime(scheduledTime);
            future = executorService.schedule(task, delay, TimeUnit.MILLISECONDS);
        } else {
            future = executorService.submit(task);
        }

        runningScripts.put(script.getId(), future);

        if (!isNonBlocking) {
            try {
                Value v = (Value) future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return script;
    }

    /**
     * Executes a given script asynchronously.
     * <p>
     * This method updates the script's status to {@code EXECUTING} before running,
     * and then to {@code COMPLETED} or {@code FAILED} based on the execution result.
     * The script's output or error messages are captured and stored in the script object.
     * Execution time is measured and recorded.
     * </p>
     *
     * @param script the script to execute. The script should contain the script code,
     *               an identifier, and fields to store execution status, output, error messages,
     *               and execution time.
     */
    @Async
    public void executeScript(Script script) {
        script.setStatus(ScriptStatus.EXECUTING);
        LocalDateTime before = LocalDateTime.now();

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errStream = new ByteArrayOutputStream();
             PrintStream outPrintStream = new PrintStream(outStream);
             PrintStream errPrintStream = new PrintStream(errStream);
             Context context = Context.newBuilder("js")
                     .out(outPrintStream)
                     .err(errPrintStream)
                     .allowAllAccess(true)
                     .build()) {

            context.eval("js", script.getScript());
            script.setOut(outStream.toString());
            script.setStatus(ScriptStatus.COMPLETED);
        } catch (Exception e) {
            script.setErr(e.getMessage());
            script.setStatus(ScriptStatus.FAILED);
        }

        LocalDateTime after = LocalDateTime.now();
        script.setExecutionTime(before.until(after, ChronoUnit.MILLIS));

        scriptsStorage.put(script.getId(), script);
        runningScripts.remove(script.getId());
    }

    /**
     * Retrieves Future object of running script and cancels it.
     * Removes Future from running scripts storage
     * and updates script execution status of script.
     *
     * @param scriptId id of the script to be stopped
     */
    public void stopExecution(long scriptId) {
        Future<?> running = runningScripts.get(scriptId);

        if (running != null)
            runningScripts.remove(scriptId);

        Script script = scriptsStorage.get(scriptId);
        if (script != null && !(script.getStatus().equals(ScriptStatus.STOPPED)
                || script.getStatus().equals(ScriptStatus.COMPLETED)
                || script.getStatus().equals(ScriptStatus.FAILED))) {
            script.setStatus(ScriptStatus.STOPPED);
            scriptsStorage.put(scriptId, script);
        } else {
            throw new RuntimeException(
                    String.format("Script: %d cannot be stopped. Script should be active or scheduled", scriptId)
            );
        }
    }


    /**
     * @param comparator sorting option, can be either by ID or by Scheduled Time in ascending or descending order.
     * @return List of scripts after sorting
     */
    public List<Script> getScripts(Comparator<Script> comparator) {
        return scriptsStorage.values()
                .stream()
                .sorted(comparator)
                .toList();
    }

    /**
     * @param scriptStatus current script status: <br>
     *  <pre>
     *   {@code
     *    COMPLETED,
     *    EXECUTING,
     *    QUEUED,
     *    SCHEDULED,
     *    STOPPED,
     *    FAILED
     *    }
     *  </pre>
     * @param comparator   sorting option, can be either by ID or by Scheduled Time in ascending or descending order.
     * @return List of scripts after filtering by script status and sorting
     */
    public List<Script> getFilteredScripts(ScriptStatus scriptStatus, Comparator<Script> comparator) {

        return scriptsStorage.values()
                .stream()
                .filter(it -> it.getStatus().equals(scriptStatus))
                .sorted(comparator)
                .toList();
    }

    /**
     * Removes all scripts with following status:<br>
     * <pre>
     *   {@code
     *   COMPLETED,
     *   STOPPED,
     *   FAILED
     *   }
     * </pre>
     *
     * @return amount of removed items
     */
    public int removeInactive() {
        Iterator<Map.Entry<Long, Script>> storageIterator = scriptsStorage.entrySet().iterator();

        Map.Entry<Long, Script> ss;
        int affectedScriptsAmount = 0;
        while (storageIterator.hasNext()) {
            ss = storageIterator.next();
            Script temp = ss.getValue();

            if (temp.getStatus().equals(ScriptStatus.FAILED) ||
                    temp.getStatus().equals(ScriptStatus.STOPPED) ||
                    temp.getStatus().equals(ScriptStatus.COMPLETED)) {
                storageIterator.remove();

                ++affectedScriptsAmount;
            }
        }

        return affectedScriptsAmount;
    }

    /**
     * Removes script by specified ID
     * @return removed script
     */
    public Optional<Script> removeById(long id) {
        Script temp = scriptsStorage.get(id);

        if (temp.getStatus().equals(ScriptStatus.FAILED) ||
                temp.getStatus().equals(ScriptStatus.STOPPED) ||
                temp.getStatus().equals(ScriptStatus.COMPLETED)) {

            return Optional.of(scriptsStorage.remove(id));
        } else {
            throw new RuntimeException(String.format("Couldn't remove %d script with status: %s", id, temp.getStatus()));
        }
    }

    public Comparator<Script> idComparator(boolean order) {
        return (o1, o2) -> (int) (o1.getId() - o2.getId()) * (order ? 1 : -1);
    }

    public Comparator<Script> scheduledTimeComparator(boolean order) {
        return (o1, o2) -> o1.getScheduledTime().compareTo(o2.getScheduledTime()) * (order ? 1 : -1);
    }
}
