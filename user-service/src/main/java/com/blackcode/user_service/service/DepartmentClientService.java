package com.blackcode.user_service.service;

import com.blackcode.user_service.dto.DepartmentDto;

public interface DepartmentClientService {
    DepartmentDto getDepartmentById(Long departmentId);
}
