package com.blackcode.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserReq {

    private String userId;

    private String nama;

    private String email;

    private Long departmentId;

    private Long addressId;
}
