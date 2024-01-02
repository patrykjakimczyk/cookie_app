package com.cookie.app.exception;

public class ModifyingProductsFromWrongPantryException extends RuntimeException {
    public ModifyingProductsFromWrongPantryException() {
        super();
    }
    public ModifyingProductsFromWrongPantryException(String message, Throwable cause) {
        super(message, cause);
    }
    public ModifyingProductsFromWrongPantryException(String message) {
        super(message);
    }
    public ModifyingProductsFromWrongPantryException(Throwable cause) {
        super(cause);
    }
}
