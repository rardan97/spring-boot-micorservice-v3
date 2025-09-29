package com.blackcode.auth_service.security.service;

import com.blackcode.auth_service.exception.TokenRefreshException;
import com.blackcode.auth_service.model.RefreshToken;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.RefreshTokenRepository;
import com.blackcode.auth_service.repository.UserAuthRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthRefreshTokenService {

    @Value("${blackcode.app.jwtRefreshExpirationMs}")
    private int refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserAuthRepository userAuthRepository;

    private final UserAuthTokenService userAuthTokenService;

    public UserAuthRefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserAuthRepository userAuthRepository, UserAuthTokenService userAuthTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAuthRepository = userAuthRepository;
        this.userAuthTokenService = userAuthTokenService;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String jwt, String userId){
        RefreshToken refreshToken = null;
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserAuthId(userId);
        if (existingToken.isPresent()) {
            refreshToken = new RefreshToken();
            refreshToken.setId(existingToken.get().getId());
            refreshToken.setUserAuth(existingToken.get().getUserAuth());
            refreshToken.setExpiryDate(existingToken.get().getExpiryDate());
            refreshToken.setToken(existingToken.get().getToken());
        }else{
            UserAuth dataUserAuth = userAuthRepository.findById(userId).get();
            refreshToken = new RefreshToken();
            refreshToken.setUserAuth(dataUserAuth);
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken = refreshTokenRepository.save(refreshToken);
        }
        userAuthTokenService.processUserAuthTokenAdd(userId, jwt);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserAuthId(String userId){
        refreshTokenRepository.deleteByUserAuth(userAuthRepository.findById(userId).get());
    }

}
