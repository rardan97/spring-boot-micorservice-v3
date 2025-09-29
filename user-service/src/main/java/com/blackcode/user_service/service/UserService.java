package com.blackcode.user_service.service;

import com.blackcode.user_service.dto.UserReq;
import com.blackcode.user_service.dto.UserRes;
import com.blackcode.user_service.dto.UserResSyn;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserRes> getAllUser();

    UserRes getUserById(String userId);

    UserResSyn addUser(UserReq userReq);

    UserRes updateUser(String userId, UserReq userReq);

    Map<String, Object> deleteUser(String userId);

}
