package com.blackcode.auth_service.exception;

public class InvalidJwtException extends RuntimeException{
    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
