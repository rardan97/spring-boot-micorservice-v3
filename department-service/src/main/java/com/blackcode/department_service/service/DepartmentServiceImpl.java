package com.blackcode.department_service.service;

import com.blackcode.department_service.dto.DepartmentReq;
import com.blackcode.department_service.dto.DepartmentRes;
import com.blackcode.department_service.exception.DataNotFoundException;
import com.blackcode.department_service.model.Department;
import com.blackcode.department_service.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService{

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<DepartmentRes> getAllDepartment() {
        List<Department> departmentList = departmentRepository.findAll();
        return departmentList.stream().map(this::mapToDepartmentRes).toList();
    }

    @Override
    public DepartmentRes getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DataNotFoundException("Department Not Found with id : "+departmentId));
        return mapToDepartmentRes(department);
    }

    @Override
    public DepartmentRes addDepartment(DepartmentReq departmentReq) {
        Department department = new Department();
        department.setDepartmentName(departmentReq.getDepartmentName());
        department.setDepartmentCode(departmentReq.getDepartmentCode());
        department.setDepartmentDescription(departmentReq.getDepartmentDescription());
        Department saveDepartment = departmentRepository.save(department);
        return mapToDepartmentRes(saveDepartment);
    }

    @Override
    public DepartmentRes updateDepartment(Long departmentId, DepartmentReq departmentReq) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DataNotFoundException("Department Not Found with id : "+ departmentId));

        department.setDepartmentName(departmentReq.getDepartmentName());
        department.setDepartmentCode(departmentReq.getDepartmentCode());
        department.setDepartmentDescription(departmentReq.getDepartmentDescription());
        Department updateDepartment = departmentRepository.save(department);
        return mapToDepartmentRes(updateDepartment);
    }

    @Override
    public Map<String, Object> deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DataNotFoundException("Department Not Found with id :"+departmentId));
        departmentRepository.deleteById(departmentId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deleteDepartmentId", departmentId);
        responseData.put("info", "The Department was removed from the database");
        return responseData;
    }

    private DepartmentRes mapToDepartmentRes(Department department){
        DepartmentRes departmentRes = new DepartmentRes();
        departmentRes.setDepartmentId(department.getDepartmentId());
        departmentRes.setDepartmentName(department.getDepartmentName());
        departmentRes.setDepartmentCode(department.getDepartmentCode());
        departmentRes.setDepartmentDescription(departmentRes.getDepartmentDescription());
        return departmentRes;
    }
}
