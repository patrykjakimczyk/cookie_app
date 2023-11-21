package com.cookie.app.exception;

public class PantryProductNotFoundException extends RuntimeException{
    public PantryProductNotFoundException() {
        super();
    }
    public PantryProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public PantryProductNotFoundException(String message) {
        super(message);
    }
    public PantryProductNotFoundException(Throwable cause) {
        super(cause);
    }
}
