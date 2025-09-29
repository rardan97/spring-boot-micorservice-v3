package com.blackcode.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentDto {

    private Long departmentId;

    private String departmentName;

    private String departmentCode;

    private String departmentDescription;

}
