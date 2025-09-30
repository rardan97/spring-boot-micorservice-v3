package com.blackcode.auth_service.service;

import com.blackcode.auth_service.dto.SignUpReq;
import com.blackcode.auth_service.dto.UserRes;
import com.blackcode.auth_service.model.UserAuth;

public interface UserClientService {

    UserRes createUser(UserAuth userAuth, SignUpReq signUpReq);
}
