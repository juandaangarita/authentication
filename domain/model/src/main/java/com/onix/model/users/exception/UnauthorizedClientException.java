package com.onix.model.users.exception;

public class UnauthorizedClientException extends RuntimeException {
    public UnauthorizedClientException(String email) {
        super("Unauthorized client with email " + email);
    }
}
