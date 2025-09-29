package com.blackcode.auth_service.exception;

public class TokenExpiredException extends RuntimeException{
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
