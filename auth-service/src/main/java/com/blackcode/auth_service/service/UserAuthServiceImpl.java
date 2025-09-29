package com.blackcode.auth_service.service;

import com.blackcode.auth_service.dto.*;
import com.blackcode.auth_service.exception.ExternalServiceException;
import com.blackcode.auth_service.exception.TokenRefreshException;
import com.blackcode.auth_service.exception.UsernameAlreadyExistsException;
import com.blackcode.auth_service.helper.TypeRefs;
import com.blackcode.auth_service.model.RefreshToken;
import com.blackcode.auth_service.model.Token;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.TokenRepository;
import com.blackcode.auth_service.repository.UserAuthRepository;
import com.blackcode.auth_service.security.jwt.JwtUtils;
import com.blackcode.auth_service.security.service.UserAuthDetailsImpl;
import com.blackcode.auth_service.security.service.UserAuthRefreshTokenService;
import com.blackcode.auth_service.security.service.UserAuthTokenService;
import com.blackcode.auth_service.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class UserAuthServiceImpl implements UserAuthService{

    private static final Logger logger = LoggerFactory.getLogger(UserAuthServiceImpl.class);

    private final PasswordEncoder encoder;

    private final AuthenticationManager authenticationManager;

    private final UserAuthRepository userAuthRepository;

    private final TokenRepository tokenRepository;

    private final UserAuthTokenService userAuthTokenService;

    private final JwtUtils jwtUtils;

    private final UserAuthRefreshTokenService userAuthRefreshTokenService;

    private static final String USER_API_PATH = "/api/user/addUser";

    private final WebClient userClient;

    public UserAuthServiceImpl(PasswordEncoder encoder,
                               AuthenticationManager authenticationManager,
                               UserAuthRepository userAuthRepository,
                               TokenRepository tokenRepository,
                               UserAuthTokenService userAuthTokenService,
                               JwtUtils jwtUtils,
                               UserAuthRefreshTokenService userAuthRefreshTokenService,
                               @Qualifier("userClient") WebClient userClient) {
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.userAuthRepository = userAuthRepository;
        this.tokenRepository = tokenRepository;
        this.userAuthTokenService = userAuthTokenService;
        this.jwtUtils = jwtUtils;
        this.userAuthRefreshTokenService = userAuthRefreshTokenService;
        this.userClient = userClient;
    }

    @Override
    public JwtRes singIn(LoginReq loginRequest) {
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
        System.out.println("username : "+signUpReq.getUsername());
        if(userAuthRepository.existsByUsername(signUpReq.getUsername())){
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }
        UserAuth authUser = new UserAuth();
        authUser.setUserId(UUID.randomUUID().toString());
        authUser.setUsername(signUpReq.getUsername());
        authUser.setPassword(encoder.encode(signUpReq.getPassword()));

        UserAuth savedUser = userAuthRepository.save(authUser);

        fetchUser(savedUser, signUpReq);

        return new MessageRes("User created: " + savedUser.getUserId());
    }

    private UserRes fetchUser(UserAuth userAuth, SignUpReq signUpReq){
        UserReq request = new UserReq();
        request.setUserId(userAuth.getUserId());
        request.setNama(signUpReq.getNama());
        request.setEmail(signUpReq.getEmail());
        request.setAddressId(signUpReq.getAddressId());
        request.setDepartmentId(signUpReq.getDepartmentId());

        return fetchExternalData(
                userClient,
                USER_API_PATH,
                TypeRefs.userDtoResponse(),
                request,
                "user-service"
        );
    }


    private <T> T fetchExternalData(WebClient client, String uri, ParameterizedTypeReference<ApiResponse<T>> typeRef, UserReq userReq, String dataType){
        try {
            ApiResponse<T> response = client.post()
                    .uri(uri)
                    .bodyValue(userReq)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            clientResponse -> Mono.error(new ExternalServiceException("Internal error on " + dataType))
                    )
                    .bodyToMono(typeRef)
                    .timeout(Duration.ofSeconds(3))
                    .onErrorResume(e -> {
                        logger.warn("{} not found for : {}", dataType, e.getMessage());
                        return Mono.error(e);
                    })
                    .block();
            if (response == null) {
                logger.warn("No response received for {}", dataType);
                throw new ExternalServiceException("No response received from " + dataType);
            }
            return response.getData();

        }catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.error("Timeout fetching {}: {}", dataType, e.getMessage());
                throw new ExternalServiceException("Timeout when calling " + dataType, e);
            }
            throw e;
        }catch (Exception e){
            logger.error("Unexpected error fetching {}: {}", dataType, e.getMessage());
            throw new ExternalServiceException("Unexpected error calling " + dataType, e);
        }
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
