package com.cookie.app.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @ApiResponse(responseCode = "400", description = "Method argument not valid")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ApiResponse(responseCode = "400", description = "Constraint violation",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))})
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> constraintViolationException(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        String message = violations.isEmpty() ?
                "ConstraintViolationException occurred." :
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }


    @ApiResponse(responseCode = "500", description = "User was not found after authentication",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(UserWasNotFoundAfterAuthException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionMessage> userWasNotFoundAfterAuthException(UserWasNotFoundAfterAuthException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ApiResponse(responseCode = "400", description = "Validation exception",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMessage> validationException(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ApiResponse(responseCode = "403", description = "User performed forbidden action",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(UserPerformedForbiddenActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ExceptionMessage> userPerformedForbiddenActionException(UserPerformedForbiddenActionException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ApiResponse(responseCode = "404", description = "Resource not found",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionMessage> resourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ApiResponse(responseCode = "400", description = "User already added to group",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(UserAlreadyAddedToGroupException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMessage> userAlreadyAddedToGroupException(UserAlreadyAddedToGroupException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }

    @ApiResponse(responseCode = "400", description = "Cannot read object",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionMessage.class))})
    @ExceptionHandler(MappingJsonToObjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMessage> mappingJsonToObjectException(MappingJsonToObjectException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(exception.getMessage(), LocalDateTime.now(clock)));
    }
}
