package com.blackcode.department_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentReq {

    private String departmentName;

    private String departmentCode;

    private String departmentDescription;
}
