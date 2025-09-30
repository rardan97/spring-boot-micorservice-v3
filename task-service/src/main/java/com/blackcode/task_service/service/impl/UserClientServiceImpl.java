package com.blackcode.task_service.service.impl;

import com.blackcode.task_service.dto.UserDto;
import com.blackcode.task_service.exception.DataNotFoundException;
import com.blackcode.task_service.helper.TypeRefs;
import com.blackcode.task_service.service.UserClientService;
import com.blackcode.task_service.utils.ApiResponse;
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

    private static final String USER_API_PATH = "/api/user/getUserById/";

    private final WebClient userClient;

    public UserClientServiceImpl(@Qualifier("userClient") WebClient userClient) {
        this.userClient = userClient;
    }

    @Override
    @Retry(name = "userService")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    public UserDto getUserById(String userId) {
        if (userId == null) return null;

        String uri = USER_API_PATH + userId;
        ParameterizedTypeReference<ApiResponse<UserDto>> typeRef = TypeRefs.userDtoResponse();
        ApiResponse<UserDto> response = userClient.get()
                .uri(uri)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    logger.info("Response status: {}", status);

                    if (status.isError()) {
                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            logger.error("Error response body: {}", errorBody);
                            return Mono.error(new DataNotFoundException("User not found"));
                        });
                    }
                    return clientResponse.bodyToMono(typeRef);
                })
                .timeout(Duration.ofSeconds(3))
                .block();

        if (response == null) {
            logger.warn("No response for User ID {}", userId);
            return null;
        }
        return response.getData();
    }

    public UserDto fallback(String userId, Throwable throwable) {
        logger.error("Failed get data User by ID {}. Error: {}", userId, throwable.toString());
        return null;
    }
}
