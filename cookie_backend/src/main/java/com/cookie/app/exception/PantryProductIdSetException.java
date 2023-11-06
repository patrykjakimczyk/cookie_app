package com.cookie.app.exception;

public class PantryProductIdSetException extends RuntimeException{
    public PantryProductIdSetException() {
        super();
    }
    public PantryProductIdSetException(String message, Throwable cause) {
        super(message, cause);
    }
    public PantryProductIdSetException(String message) {
        super(message);
    }
    public PantryProductIdSetException(Throwable cause) {
        super(cause);
    }
}
