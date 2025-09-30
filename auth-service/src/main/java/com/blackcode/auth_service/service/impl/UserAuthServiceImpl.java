package com.blackcode.auth_service.service.impl;

import com.blackcode.auth_service.dto.*;
import com.blackcode.auth_service.exception.TokenRefreshException;
import com.blackcode.auth_service.exception.UserServiceUnavailableException;
import com.blackcode.auth_service.exception.UsernameAlreadyExistsException;
import com.blackcode.auth_service.model.RefreshToken;
import com.blackcode.auth_service.model.Token;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.TokenRepository;
import com.blackcode.auth_service.repository.UserAuthRepository;
import com.blackcode.auth_service.security.jwt.JwtUtils;
import com.blackcode.auth_service.security.service.UserAuthDetailsImpl;
import com.blackcode.auth_service.security.service.UserAuthRefreshTokenService;
import com.blackcode.auth_service.security.service.UserAuthTokenService;
import com.blackcode.auth_service.service.UserAuthService;
import com.blackcode.auth_service.service.UserClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthServiceImpl.class);

    private final PasswordEncoder encoder;

    private final AuthenticationManager authenticationManager;

    private final UserAuthRepository userAuthRepository;

    private final TokenRepository tokenRepository;

    private final UserAuthTokenService userAuthTokenService;

    private final JwtUtils jwtUtils;

    private final UserAuthRefreshTokenService userAuthRefreshTokenService;

    private final UserClientService userClientService;

    public UserAuthServiceImpl(PasswordEncoder encoder,
                               AuthenticationManager authenticationManager,
                               UserAuthRepository userAuthRepository,
                               TokenRepository tokenRepository,
                               UserAuthTokenService userAuthTokenService,
                               JwtUtils jwtUtils,
                               UserAuthRefreshTokenService userAuthRefreshTokenService, UserClientService userClientService) {
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.userAuthRepository = userAuthRepository;
        this.tokenRepository = tokenRepository;
        this.userAuthTokenService = userAuthTokenService;
        this.jwtUtils = jwtUtils;
        this.userAuthRefreshTokenService = userAuthRefreshTokenService;
        this.userClientService = userClientService;
    }

    @Override
    public JwtRes signIn(LoginReq loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserAuthDetailsImpl userAuthDetails = (UserAuthDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtTokenUserAuth(userAuthDetails);
        userAuthTokenService.processUserAuthTokenAdd(userAuthDetails.getUserId(), jwt);

        RefreshToken refreshToken = userAuthRefreshTokenService.createRefreshToken(
                jwt,
                userAuthDetails.getUserId()
        );

        logger.info("User {} signed in successfully", userAuthDetails.getUsername());

        return new JwtRes(
                jwt,
                refreshToken.getToken(),
                userAuthDetails.getUserId(),
                userAuthDetails.getUsername()
        );
    }

    @Transactional
    @Override
    public MessageRes signUp(SignUpReq signUpReq) {
        logger.info("username : {}", signUpReq.getUsername());
        if(userAuthRepository.existsByUsername(signUpReq.getUsername())){
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }
        UserAuth authUser = new UserAuth();
        authUser.setUserId(UUID.randomUUID().toString());
        authUser.setUsername(signUpReq.getUsername());
        authUser.setPassword(encoder.encode(signUpReq.getPassword()));
        UserAuth savedUser = userAuthRepository.save(authUser);

        try {
            UserRes userRes = userClientService.createUser(savedUser, signUpReq);

            if (userRes == null) {
                logger.error("User service returned null response for userId: {}", savedUser.getUserId());
                throw new UserServiceUnavailableException("Failed to call user-service for userId " + savedUser.getUserId());
            }

        } catch (Exception e) {
            logger.error("Failed to create user in user-service for userId: {}", savedUser.getUserId(), e);
            throw new UserServiceUnavailableException("Failed to call user-service" + savedUser.getUserId(), e); // Trigger rollback
        }

        return new MessageRes("User created: " + savedUser.getUserId());
    }

    @Override
    public TokenRefreshRes refreshToken(TokenRefreshReq request) {
        TokenRefreshRes tokenRefreshRes = null;
        String requestRefreshToken = request.getRefreshToken();
        Optional<RefreshToken> refreshToken = userAuthRefreshTokenService.findByToken(requestRefreshToken);
        if(refreshToken.isPresent()){
            RefreshToken refreshToken1 = refreshToken.get();
            refreshToken1 = userAuthRefreshTokenService.verifyExpiration(refreshToken1);
            UserAuth userAuth = refreshToken1.getUserAuth();
            String token = jwtUtils.generateTokenFromUsername(userAuth.getUsername());
            userAuthTokenService.processStaffTokenRefresh(userAuth.getUsername(), token);
            tokenRefreshRes = new TokenRefreshRes(token, requestRefreshToken);
        }else {
            throw new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!");
        }
        return tokenRefreshRes;
    }

    @Override
    public MessageRes signOut(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String headerAuth = request.getHeader("Authorization");
            if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
                String jwtToken = headerAuth.substring(7);
                UserAuthDetailsImpl userAuthDetails = (UserAuthDetailsImpl) authentication.getPrincipal();
                String userId = userAuthDetails.getUserId();

                Optional<Token> userTokenData = tokenRepository.findByToken(jwtToken);
                if (userTokenData.isPresent()) {
                    userAuthRefreshTokenService.deleteByUserAuthId(userId);
                    Token token = userTokenData.get();
                    token.setIsActive(false);
                    tokenRepository.save(token);
                    return new MessageRes("Logout successful!");
                } else {
                    return new MessageRes("Token not found, logout failed!");
                }
            } else {
                return new MessageRes("Authorization header is missing or invalid");
            }
        } else {
            return new MessageRes("User is not authenticated");
        }
    }
}
