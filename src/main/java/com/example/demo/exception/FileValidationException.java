package com.example.demo.exception;

public class FileValidationException extends RuntimeException {

    public FileValidationException(String message) {
        super(message);
    }
}
