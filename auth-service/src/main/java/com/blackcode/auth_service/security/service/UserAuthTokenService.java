package com.blackcode.auth_service.security.service;

import com.blackcode.auth_service.model.Token;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.TokenRepository;
import com.blackcode.auth_service.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserAuthTokenService {

    @Value("${blackcode.app.jwtRefreshExpirationMs}")
    private int refreshTokenDurationMs;

    @Value("${blackcode.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private final TokenRepository tokenRepository;

    private final UserAuthRepository userAuthRepository;

    public UserAuthTokenService(TokenRepository tokenRepository, UserAuthRepository userAuthRepository) {
        this.tokenRepository = tokenRepository;
        this.userAuthRepository = userAuthRepository;
    }

    public void processUserAuthTokenAdd(String userId, String jwt){
        Date date = new Date((new Date()).getTime() + jwtExpirationMs);
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Optional<Token> userAuthTokenData = tokenRepository.findByUserId(userId);
        if(userAuthTokenData.isPresent()){
            userAuthTokenData.get().setToken(jwt);
            userAuthTokenData.get().setIsActive(true);
            userAuthTokenData.get().setExpiryDate(localDateTime);
            userAuthTokenData.get().setUpdatedAt(LocalDateTime.now());
            tokenRepository.save(userAuthTokenData.get());
        }else{
            Token token = new Token();
            token.setToken(jwt);
            token.setUserId(userId);
            token.setIsActive(true);
            token.setExpiryDate(localDateTime);
            token.setCreatedAt(LocalDateTime.now());
            token.setUpdatedAt(LocalDateTime.now());
            tokenRepository.save(token);
        }
    }

    public void processStaffTokenRefresh(String userName, String jwt){
        Date date = new Date((new Date()).getTime() + jwtExpirationMs);
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Optional<UserAuth> dataUserAuth = userAuthRepository.findByUsername(userName);
        if(dataUserAuth.isPresent()){
            Optional<Token> userAuthTokenData = tokenRepository.findByUserId(dataUserAuth.get().getUserId());
            if(userAuthTokenData.isPresent()){
                userAuthTokenData.get().setToken(jwt);
                userAuthTokenData.get().setIsActive(true);
                userAuthTokenData.get().setExpiryDate(localDateTime);
                userAuthTokenData.get().setUpdatedAt(LocalDateTime.now());
                tokenRepository.save(userAuthTokenData.get());
            }
        }
    }
}
