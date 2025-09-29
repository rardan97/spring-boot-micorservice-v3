package com.blackcode.auth_service.exception;

import com.blackcode.auth_service.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(DataNotFoundException.class)
//    public ResponseEntity<ApiResponse<Object>> handleDataNotFound(DataNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
//    }

//    @ExceptionHandler(DuplicateResourceException.class)
//    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
//    }

//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
//    }

//    @ExceptionHandler(UsernameAlreadyExistsException.class)
//    public ResponseEntity<ApiResponse<Object>> handleUsernameExists(UsernameAlreadyExistsException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
//    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenRefreshException(TokenRefreshException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler({InvalidJwtException.class, TokenExpiredException.class})
    public ResponseEntity<ApiResponse<Object>> handleJwtExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountExpired(AccountExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Account has expired", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is locked", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is disabled", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllUncaughtException(Exception ex) {
        ex.printStackTrace(); // bisa diganti dengan logger.error(...)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

}
