package com.example.demo.exception;

public class OpenIdValidationException extends RuntimeException {

    public OpenIdValidationException(String message) {
        super(message);
    }

    public OpenIdValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
