package com.blackcode.auth_service.controller;

import com.blackcode.auth_service.model.TokenBlacklist;
import com.blackcode.auth_service.repository.TokenBlacklistRepository;
import com.blackcode.auth_service.service.TokenBlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/blacklist")
public class TokenBlacklistController {

    private final TokenBlacklistService tokenBlacklistService;

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistController(TokenBlacklistService tokenBlacklistService, TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Boolean> isTokenBlacklisted(@PathVariable String token) {
        Optional<TokenBlacklist> tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        if (tokenBlacklist.isPresent() && !tokenBlacklist.get().getIsActive()) {
            return ResponseEntity.ok(true); // Token sudah diblacklist
        }
        return ResponseEntity.ok(false); // Token tidak diblacklist
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String token) {
        TokenBlacklist blacklistToken = new TokenBlacklist();
        blacklistToken.setToken(token);
        blacklistToken.setIsActive(false);
        blacklistToken.setExpiryDate(LocalDateTime.now().plusDays(30));  // Misalnya expired setelah 30 hari
        tokenBlacklistRepository.save(blacklistToken);

        return ResponseEntity.ok("Token logged out and added to blacklist.");
    }
}
