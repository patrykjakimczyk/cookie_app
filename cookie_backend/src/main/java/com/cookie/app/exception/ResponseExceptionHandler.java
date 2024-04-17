package com.cookie.app.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import java.util.Set;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> constraintViolationException(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        String message = "";

        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            violations.forEach(violation -> builder.append(violation.getMessage()).append("\n"));
            message = builder.toString();
        } else {
            message = "ConstraintViolationException occured.";
        }
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionMessage> nullPointerException(NotUniqueValueException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage("Unindentified problem occured during request processing", Instant.now()));
    }

    @ExceptionHandler(NotUniqueValueException.class)
    public ResponseEntity<ExceptionMessage> notUniqueSqlException(NotUniqueValueException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UserWasNotFoundAfterAuthException.class)
    public ResponseEntity<ExceptionMessage> userWasNotFoundAfterAuthException(UserWasNotFoundAfterAuthException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionMessage> validationException(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UserPerformedForbiddenActionException.class)
    public ResponseEntity<ExceptionMessage> userPerformedForbiddenActionException(UserPerformedForbiddenActionException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UserAlreadyAddedToGroupException.class)
    public ResponseEntity<ExceptionMessage> userAlreadyAddedToGroupException(UserAlreadyAddedToGroupException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), Instant.now()));
    }
}
