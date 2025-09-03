package com.onix.model.users.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials provided.");
    }

    public InvalidCredentialsException(String message) {
        super("Invalid credentials. " + message);
    }
}
