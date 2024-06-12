package api.demo.graalvmdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ExceptionResponse {
    private HttpStatusCode status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    private Map<String, String> causes;


    public ExceptionResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ExceptionResponse(HttpStatusCode status) {
        this();
        this.status = status;
    }

    public ExceptionResponse(HttpStatusCode status, Map<String, String> causes) {
        this(status);
        this.causes = causes;
    }
}
