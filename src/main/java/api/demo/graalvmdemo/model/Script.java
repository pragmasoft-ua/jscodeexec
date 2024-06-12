package api.demo.graalvmdemo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Script {

    private long id;

    private String script;

    private ScriptStatus status;

    private String out;

    private String err;

    private LocalDateTime scheduledTime;

    private Long executionTime;

    public Script(String script) {
        this.script = script;
        this.status = ScriptStatus.QUEUED;
    }

}
