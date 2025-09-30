package com.blackcode.user_service.service.impl;

import com.blackcode.user_service.dto.AddressDto;
import com.blackcode.user_service.dto.DepartmentDto;
import com.blackcode.user_service.exception.DataNotFoundException;
import com.blackcode.user_service.helper.TypeRefs;
import com.blackcode.user_service.service.AddressClientService;
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
public class AddressClientServiceImpl implements AddressClientService {

    private static final Logger logger = LoggerFactory.getLogger(AddressClientServiceImpl.class);

    private static final String ADDRESS_API_PATH = "/api/address/getAddressById/";

    private final WebClient addressClient;

    public AddressClientServiceImpl(@Qualifier("addressClient") WebClient addressClient) {
        this.addressClient = addressClient;
    }

    @Override
    @Retry(name = "addressService")
    @CircuitBreaker(name = "addressService", fallbackMethod = "fallback")
    public AddressDto getAddressById(Long addressId) {
        if (addressId == null) return null;

        String uri = ADDRESS_API_PATH + addressId;

        ParameterizedTypeReference<ApiResponse<AddressDto>> typeRef = TypeRefs.addressDtoResponse();

        ApiResponse<AddressDto> response = addressClient.get()
                .uri(uri)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    logger.info("Response status: {}", status);

                    if (status.isError()) {
                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            logger.error("Error response body: {}", errorBody);
                            return Mono.error(new DataNotFoundException("Address not found"));
                        });
                    }

                    return clientResponse.bodyToMono(typeRef);
                })
                .timeout(Duration.ofSeconds(3))
                .block();

        if (response == null) {
            logger.warn("No response for address ID {}", addressId);
            return null;
        }
        return response.getData();
    }

    public AddressDto fallback(Long addressId, Throwable throwable) {
        logger.error("Gagal ambil data address untuk ID {}. Error: {}", addressId, throwable.toString());
        return null;
    }
}
