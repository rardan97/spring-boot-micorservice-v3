package com.blackcode.user_service.service.impl;

import com.blackcode.user_service.dto.DepartmentDto;
import com.blackcode.user_service.exception.DataNotFoundException;
import com.blackcode.user_service.helper.TypeRefs;
import com.blackcode.user_service.service.DepartmentClientService;
import com.blackcode.user_service.utils.ApiResponse;
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
public class DepartmentClientServiceImpl implements DepartmentClientService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentClientServiceImpl.class);

    private static final String DEPARTMENT_API_PATH = "/api/department/getDepartmentById/";

    private final WebClient departmentClient;

    public DepartmentClientServiceImpl(@Qualifier("departmentClient") WebClient departmentClient) {
        this.departmentClient = departmentClient;
    }

    @Override
    @Retry(name = "departmentService")
    @CircuitBreaker(name = "departmentService", fallbackMethod = "fallback")
    public DepartmentDto getDepartmentById(Long departmentId) {
        if (departmentId == null) return null;

        String uri = DEPARTMENT_API_PATH + departmentId;

        ParameterizedTypeReference<ApiResponse<DepartmentDto>> typeRef = TypeRefs.departmentDtoResponse();

        ApiResponse<DepartmentDto> response = departmentClient.get()
                .uri(uri)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    logger.info("Response status: {}", status);

                    if (status.isError()) {
                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            logger.error("Error response body: {}", errorBody);
                            return Mono.error(new DataNotFoundException("Department not found"));
                        });
                    }

                    return clientResponse.bodyToMono(typeRef);
                })
                .timeout(Duration.ofSeconds(3))
                .block();

        if (response == null) {
            logger.warn("No response for department ID {}", departmentId);
            return null;
        }
        return response.getData();
    }

    public DepartmentDto fallback(Long departmentId, Throwable throwable) {
        logger.error("Gagal ambil data department untuk ID {}. Error: {}", departmentId, throwable.toString());
        return null;
    }
}
