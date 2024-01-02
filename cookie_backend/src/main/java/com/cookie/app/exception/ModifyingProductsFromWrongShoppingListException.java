package com.cookie.app.exception;

public class ModifyingProductsFromWrongShoppingListException extends RuntimeException {
    public ModifyingProductsFromWrongShoppingListException() {
        super();
    }
    public ModifyingProductsFromWrongShoppingListException(String message, Throwable cause) {
        super(message, cause);
    }
    public ModifyingProductsFromWrongShoppingListException(String message) {
        super(message);
    }
    public ModifyingProductsFromWrongShoppingListException(Throwable cause) {
        super(cause);
    }
}
