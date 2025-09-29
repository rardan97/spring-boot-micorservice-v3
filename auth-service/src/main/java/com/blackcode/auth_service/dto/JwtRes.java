package com.blackcode.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtRes {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private String userId;
    private String username;

    public JwtRes(String token, String refreshToken, String userId, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
    }
}
