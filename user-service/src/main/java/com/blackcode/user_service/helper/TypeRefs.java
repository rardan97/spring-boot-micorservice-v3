package com.blackcode.user_service.helper;

import com.blackcode.user_service.dto.AddressDto;
import com.blackcode.user_service.dto.DepartmentDto;
import com.blackcode.user_service.utils.ApiResponse;
import org.springframework.core.ParameterizedTypeReference;

public class TypeRefs {
    public static ParameterizedTypeReference<ApiResponse<DepartmentDto>> departmentDtoResponse() {
        return new ParameterizedTypeReference<>() {};
    }

    public static ParameterizedTypeReference<ApiResponse<AddressDto>> addressDtoResponse() {
        return new ParameterizedTypeReference<>() {};
    }
}
