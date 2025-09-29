package com.blackcode.task_service.helper;


import com.blackcode.task_service.dto.UserDto;
import com.blackcode.task_service.utils.ApiResponse;
import org.springframework.core.ParameterizedTypeReference;

public class TypeRefs {
    public static ParameterizedTypeReference<ApiResponse<UserDto>> userDtoResponse() {
        return new ParameterizedTypeReference<>() {};
    }

}
