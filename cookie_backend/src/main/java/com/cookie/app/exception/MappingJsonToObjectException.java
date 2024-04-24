package com.cookie.app.exception;

public class MappingJsonToObjectException extends RuntimeException {
    public MappingJsonToObjectException() {
        super();
    }
    public MappingJsonToObjectException(String message, Throwable cause) {
        super(message, cause);
    }
    public MappingJsonToObjectException(String message) {
        super(message);
    }
    public MappingJsonToObjectException(Throwable cause) {
        super(cause);
    }
}
