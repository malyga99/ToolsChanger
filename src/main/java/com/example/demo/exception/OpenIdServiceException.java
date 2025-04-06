package com.example.demo.exception;

public class OpenIdServiceException extends RuntimeException {

    public OpenIdServiceException(String message) {
        super(message);
    }

    public OpenIdServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
