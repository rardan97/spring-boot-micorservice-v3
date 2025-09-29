package com.blackcode.auth_service.service;

import java.time.LocalDateTime;

public interface TokenBlacklistService {

    void addToBlacklist(String token, String userId, LocalDateTime expiryDate);

    boolean isTokenBlacklisted(String token);

    void cleanUpExpiredTokens();
}
