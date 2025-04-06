package com.example.demo.exception;

public class UserDontHavePermissionException extends RuntimeException {

    public UserDontHavePermissionException(String message) {
        super(message);
    }
}
