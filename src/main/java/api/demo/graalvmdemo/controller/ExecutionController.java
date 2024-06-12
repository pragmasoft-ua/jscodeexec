package api.demo.graalvmdemo.controller;

import api.demo.graalvmdemo.model.ExecutionRequest;
import api.demo.graalvmdemo.model.Script;
import api.demo.graalvmdemo.model.ScriptStatus;
import api.demo.graalvmdemo.service.ScriptService;
import api.demo.graalvmdemo.util.TimeParser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Scripts Executor")
public class ExecutionController {

    private final ScriptService scriptService;


    @PostMapping("/non-blocking/submit-new")
    public ResponseEntity<Script> submitScriptAsync(@RequestBody ExecutionRequest script,
                                                    @RequestParam(required = false) String scheduledTime) {

        LocalDateTime scheduledDateTime = TimeParser.parseString(scheduledTime);


        return ResponseEntity.ok(scriptService.submitScript(script.getScript(), true, scheduledDateTime));
    }


    @PostMapping("/blocking/submit-new")
    public ResponseEntity<Script> submitScript(@RequestBody ExecutionRequest script,
                                               @RequestParam(required = false) String scheduledTime) {

        LocalDateTime scheduledDateTime = TimeParser.parseString(scheduledTime);


        return ResponseEntity.ok(scriptService.submitScript(script.getScript(), false, scheduledDateTime));
    }


    @GetMapping("/find-all")
    public ResponseEntity<List<Script>> findAllComparingId(@RequestParam(defaultValue = "false") boolean ascending) {
        return ResponseEntity.ok(
                scriptService.getScripts(
                        scriptService.idComparator(ascending)
                )
        );
    }


    @GetMapping("/find-by-status-comparing-by-id")
    public ResponseEntity<List<Script>> findByStatusComparingId(@RequestParam ScriptStatus scriptStatus,
                                                                @RequestParam(defaultValue = "false") boolean ascending) {
        return ResponseEntity.ok(
                scriptService.getFilteredScripts(
                        scriptStatus,
                        scriptService.idComparator(ascending)
                )
        );
    }


    @GetMapping("/find-by-status-comparing-by-scheduled-time")
    public ResponseEntity<List<Script>> findByStatusComparingTime(@RequestParam ScriptStatus scriptStatus,
                                                                  @RequestParam(defaultValue = "false") boolean ascending) {
        return ResponseEntity.ok(
                scriptService.getFilteredScripts(
                        scriptStatus,
                        scriptService.scheduledTimeComparator(ascending)
                )
        );
    }


    @PatchMapping("/stop")
    public ResponseEntity<String> stopScript(@RequestParam long scriptId) {
        scriptService.stopExecution(scriptId);

        return ResponseEntity.ok(String.format("Script %d has been stopped successfully", scriptId));
    }


    @DeleteMapping("/remove-inactive")
    public ResponseEntity<String> deleteInactiveScripts() {
        int affected = scriptService.removeInactive();

        return ResponseEntity.ok(String.format("Successfully deleted: %d records", affected));
    }


    @DeleteMapping("/remove-inactive-by-id")
    public ResponseEntity<String> deleteInactiveScriptById(@RequestParam long id) {
        Optional<Script> script = scriptService.removeById(id);

        if (script.isPresent())
            return ResponseEntity.ok(String.format("Successfully deleted script: %d", id));

        return ResponseEntity.ok(String.format("Cannot deleted %d script", id));
    }
}
