package com.blackcode.auth_service.exception;

public class UserServiceUnavailableException extends RuntimeException{
    public UserServiceUnavailableException(String message) {
        super(message);
    }

    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
