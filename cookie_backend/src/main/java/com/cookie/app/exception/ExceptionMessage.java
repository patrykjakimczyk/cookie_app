package com.cookie.app.exception;

import java.time.Instant;

public record ExceptionMessage(String message, String cause, Instant time) {}
