package com.cookie.app.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {
    private final Clock clock;

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
        String message = violations.isEmpty() ?
                "ConstraintViolationException occurred." :
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(UserWasNotFoundAfterAuthException.class)
    public ResponseEntity<ExceptionMessage> userWasNotFoundAfterAuthException(UserWasNotFoundAfterAuthException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionMessage> validationException(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ExceptionHandler(UserPerformedForbiddenActionException.class)
    public ResponseEntity<ExceptionMessage> userPerformedForbiddenActionException(UserPerformedForbiddenActionException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ExceptionHandler(UserAlreadyAddedToGroupException.class)
    public ResponseEntity<ExceptionMessage> userAlreadyAddedToGroupException(UserAlreadyAddedToGroupException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ExceptionHandler(MappingJsonToObjectException.class)
    public ResponseEntity<ExceptionMessage> mappingJsonToObjectException(MappingJsonToObjectException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }
}
