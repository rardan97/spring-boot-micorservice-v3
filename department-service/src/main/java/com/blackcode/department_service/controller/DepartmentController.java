package com.blackcode.department_service.controller;


import com.blackcode.department_service.dto.DepartmentReq;
import com.blackcode.department_service.dto.DepartmentRes;
import com.blackcode.department_service.service.DepartmentService;
import com.blackcode.department_service.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/getAllDepartment")
    public ResponseEntity<ApiResponse<List<DepartmentRes>>> getAllDepartment() {
        List<DepartmentRes> addressRes = departmentService.getAllDepartment();
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", 200, addressRes));
    }

    @GetMapping("/getDepartmentById/{id}")
    public ResponseEntity<ApiResponse<DepartmentRes>> getDepartmentById(@PathVariable("id") Long id){
        DepartmentRes addressRes = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department found",200, addressRes));
    }

    @PostMapping("/addDepartment")
    public ResponseEntity<ApiResponse<DepartmentRes>> addDepartment(@RequestBody DepartmentReq departmentReq){
        DepartmentRes addressRes = departmentService.addDepartment(departmentReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created", 201, addressRes));
    }

    @PutMapping("/updateDepartment/{id}")
    public ResponseEntity<ApiResponse<DepartmentRes>> updateDepartment(@PathVariable("id") Long id, @RequestBody DepartmentReq departmentReq){
        DepartmentRes addressRes = departmentService.updateDepartment(id, departmentReq);
        return ResponseEntity.ok(ApiResponse.success("Department Update", 200, addressRes));
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteDepartment(@PathVariable("id") Long id){
        Map<String, Object> rtn = departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", 200, rtn));
    }
}
