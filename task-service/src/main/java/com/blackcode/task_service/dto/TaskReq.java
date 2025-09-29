package com.blackcode.task_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskReq {

    private String taskName;

    private String taskDescription;

    private String taskUserId;
}
