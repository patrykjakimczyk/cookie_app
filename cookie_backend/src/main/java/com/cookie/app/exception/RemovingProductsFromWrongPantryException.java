package com.cookie.app.exception;

public class RemovingProductsFromWrongPantryException extends RuntimeException{
    public RemovingProductsFromWrongPantryException() {
        super();
    }
    public RemovingProductsFromWrongPantryException(String message, Throwable cause) {
        super(message, cause);
    }
    public RemovingProductsFromWrongPantryException(String message) {
        super(message);
    }
    public RemovingProductsFromWrongPantryException(Throwable cause) {
        super(cause);
    }
}
