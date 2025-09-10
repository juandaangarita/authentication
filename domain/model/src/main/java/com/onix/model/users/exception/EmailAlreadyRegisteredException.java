package com.onix.model.users.exception;

public class EmailAlreadyRegisteredException extends RuntimeException{
    public EmailAlreadyRegisteredException(String email) {
        super("Email " + email + " already registered.");
    }
}
