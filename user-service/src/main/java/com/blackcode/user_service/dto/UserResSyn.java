package com.blackcode.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResSyn {

    private String userId;

    private String nama;

    private String email;

    private String department;

    private String address;

}
