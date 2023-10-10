package com.cookie.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotUniqueValueException.class)
    public ResponseEntity<ExceptionMessage> notUniqueSqlException(NotUniqueValueException exception, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionMessage(exception.getMessage(), exception.getCause().getLocalizedMessage(), Instant.now()));
    }
}
