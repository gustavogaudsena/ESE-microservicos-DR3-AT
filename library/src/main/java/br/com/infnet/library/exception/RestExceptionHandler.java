package br.com.infnet.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({NoCreditAvailableException.class, BookNotAvailableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(RuntimeException ex) {
        return Map.of(
                "error", ex.getMessage(),
                "status", 400
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(UserNotFoundException ex) {
        return Map.of(
                "error", ex.getMessage(),
                "status", 404
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleInternalError(Exception ex) {
        return Map.of(
                "error", "Unexpected error: " + ex.getMessage(),
                "status", 500
        );
    }

}