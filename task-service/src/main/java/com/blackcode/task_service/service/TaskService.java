package com.blackcode.task_service.service;

import com.blackcode.task_service.dto.TaskReq;
import com.blackcode.task_service.dto.TaskRes;

import java.util.List;
import java.util.Map;

public interface TaskService {

    List<TaskRes> getAllTask();

    TaskRes getTaskById(Long taskId);

    TaskRes addTask(TaskReq taskReq);

    TaskRes updateTask(Long taskId, TaskReq taskReq);

    Map<String, Object> deleteTask(Long taskId);

}
