package com.cookie.app.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ExceptionMessage(
        @Schema(example = "You have no permissions to do that")
        String message,
        LocalDateTime time) {}
