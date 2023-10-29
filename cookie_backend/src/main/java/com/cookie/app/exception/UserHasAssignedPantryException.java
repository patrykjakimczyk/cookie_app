package com.cookie.app.exception;

public class UserHasAssignedPantryException extends RuntimeException{
    public UserHasAssignedPantryException() {
        super();
    }
    public UserHasAssignedPantryException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserHasAssignedPantryException(String message) {
        super(message);
    }
    public UserHasAssignedPantryException(Throwable cause) {
        super(cause);
    }
}
