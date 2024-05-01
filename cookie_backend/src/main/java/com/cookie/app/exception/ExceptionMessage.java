package com.cookie.app.exception;

import java.time.LocalDateTime;

public record ExceptionMessage(String message, LocalDateTime time) {}
