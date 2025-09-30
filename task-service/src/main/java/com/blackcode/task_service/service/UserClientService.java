package com.blackcode.task_service.service;

import com.blackcode.task_service.dto.UserDto;

public interface UserClientService {
    UserDto getUserById(String userId);
}
