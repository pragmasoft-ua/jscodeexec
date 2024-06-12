package api.demo.graalvmdemo.controller;

import api.demo.graalvmdemo.model.ExceptionResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {

        ExceptionResponse response = new ExceptionResponse(status,
                Map.of(ex.getClass().getSimpleName(), ex.getLocalizedMessage()));

        return buildResponseEntity(response);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleScriptStatusMismatch(RuntimeException ex) {
        ExceptionResponse response = new ExceptionResponse(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()),
                Map.of(ex.getClass().getSimpleName(), ex.getLocalizedMessage()));

        return buildResponseEntity(response);
    }

    private ResponseEntity<Object> buildResponseEntity(ExceptionResponse exceptionResponse) {
        return new ResponseEntity<>(exceptionResponse, exceptionResponse.getStatus());
    }
}
