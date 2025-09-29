package com.blackcode.department_service.service;

import com.blackcode.department_service.dto.DepartmentReq;
import com.blackcode.department_service.dto.DepartmentRes;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    List<DepartmentRes> getAllDepartment();

    DepartmentRes getDepartmentById(Long departmentId);

    DepartmentRes addDepartment(DepartmentReq departmentReq);

    DepartmentRes updateDepartment(Long departmentId, DepartmentReq departmentReq);

    Map<String, Object> deleteDepartment(Long departmentId);


}
