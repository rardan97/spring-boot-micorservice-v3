package com.blackcode.auth_service.helper;


import com.blackcode.auth_service.dto.UserRes;
import com.blackcode.auth_service.utils.ApiResponse;
import org.springframework.core.ParameterizedTypeReference;

public class TypeRefs {

    public static ParameterizedTypeReference<ApiResponse<UserRes>> userDtoResponse() {
        return new ParameterizedTypeReference<>() {};
    }

}
