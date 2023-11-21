package com.cookie.app.exception;

public class InvalidPantryProductDataException extends RuntimeException{
    public InvalidPantryProductDataException() {
        super();
    }
    public InvalidPantryProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidPantryProductDataException(String message) {
        super(message);
    }
    public InvalidPantryProductDataException(Throwable cause) {
        super(cause);
    }
}
