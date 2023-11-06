package com.cookie.app.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(NotUniqueValueException.class)
    public ResponseEntity<ExceptionMessage> notUniqueSqlException(NotUniqueValueException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UserHasAssignedPantryException.class)
    public ResponseEntity<ExceptionMessage> userHasAssignedPantryException(UserHasAssignedPantryException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UserWasNotFoundAfterAuthException.class)
    public ResponseEntity<ExceptionMessage> userWasNotFoundAfterAuthException(UserWasNotFoundAfterAuthException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(PantryNotFoundException.class)
    public ResponseEntity<ExceptionMessage> pantryNotFoundException(PantryNotFoundException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(PantryProductIdSetException.class)
    public ResponseEntity<ExceptionMessage> pantryProductIdSetException(PantryProductIdSetException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }
}
