package com.blackcode.auth_service.service.impl;

import com.blackcode.auth_service.dto.SignUpReq;
import com.blackcode.auth_service.dto.UserReq;
import com.blackcode.auth_service.dto.UserRes;
import com.blackcode.auth_service.exception.ExternalServiceException;
import com.blackcode.auth_service.helper.TypeRefs;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.service.UserClientService;
import com.blackcode.auth_service.utils.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserClientServiceImpl implements UserClientService {

    private static final Logger logger = LoggerFactory.getLogger(UserClientServiceImpl.class);

    private static final String USER_API_PATH = "/api/user/addUser";

    private final WebClient userClient;

    public UserClientServiceImpl(@Qualifier("userClient") WebClient userClient) {
        this.userClient = userClient;
    }

    @Override
    @Retry(name = "userService")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    public UserRes createUser(UserAuth userAuth, SignUpReq signUpReq) {

        if (userAuth == null || signUpReq == null || userAuth.getUserId() == null) {
            throw new IllegalArgumentException("User request not valid");
        }

        UserReq request = new UserReq();
        request.setUserId(userAuth.getUserId());
        request.setNama(signUpReq.getNama());
        request.setEmail(signUpReq.getEmail());
        request.setAddressId(signUpReq.getAddressId());
        request.setDepartmentId(signUpReq.getDepartmentId());

        String uri = USER_API_PATH;
        ParameterizedTypeReference<ApiResponse<UserRes>> typeRef = TypeRefs.userDtoResponse();

        ApiResponse<UserRes> response = userClient.post()
                .uri(uri)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    logger.info("Response status: {}", status);

                    if (status.isError()) {

                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            logger.error("Error response body: {}", errorBody);
                            return Mono.error(new ExternalServiceException("Error from user-service: "+errorBody));
                        });
                    }

                    return clientResponse.bodyToMono(typeRef);
                })
                .onErrorResume(e -> {
                    logger.error("Error creating user: {}", e.getMessage());
                    return Mono.error(new ExternalServiceException("Failed to call user-service", e));
                })
                .timeout(Duration.ofSeconds(3))
                .block();

        if (response == null) {
            logger.warn("No response for user-service");
            throw new ExternalServiceException("No response from user-service");
        }
        return response.getData();
    }

    public UserRes fallback(UserAuth userAuth, SignUpReq signUpReq, Throwable throwable) {
        logger.error("Fallback triggered saat create user. Error: {}", throwable.toString());
        return null;
    }
}
