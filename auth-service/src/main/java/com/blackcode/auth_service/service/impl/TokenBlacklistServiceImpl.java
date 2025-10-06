package com.blackcode.auth_service.service.impl;

import com.blackcode.auth_service.model.TokenBlacklist;
import com.blackcode.auth_service.repository.TokenBlacklistRepository;
import com.blackcode.auth_service.service.TokenBlacklistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistServiceImpl(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    public void addToBlacklist(String token, String userId, LocalDateTime expiryDate) {
        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setToken(token);
        tokenBlacklist.setUserId(userId);
        tokenBlacklist.setIsActive(false);  // Set as inactive after logout
        tokenBlacklist.setExpiryDate(expiryDate);
        tokenBlacklistRepository.save(tokenBlacklist);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Optional<TokenBlacklist> tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        return tokenBlacklist.isPresent() && !tokenBlacklist.get().getIsActive();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Override
    public void cleanUpExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
