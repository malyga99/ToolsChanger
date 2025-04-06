package com.example.demo.exception;

public class ToolNotFoundException extends RuntimeException {

    public ToolNotFoundException(String message) {
        super(message);
    }
}
