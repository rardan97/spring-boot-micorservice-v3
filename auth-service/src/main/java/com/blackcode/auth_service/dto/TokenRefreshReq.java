package com.blackcode.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshReq {
    @NotBlank
    private String refreshToken;
}
