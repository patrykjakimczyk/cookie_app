package com.cookie.app.exception;

public class InvalidProductDataException extends RuntimeException{
    public InvalidProductDataException() {
        super();
    }
    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidProductDataException(String message) {
        super(message);
    }
    public InvalidProductDataException(Throwable cause) {
        super(cause);
    }
}
