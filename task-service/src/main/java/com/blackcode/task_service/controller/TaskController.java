package com.blackcode.task_service.controller;

import com.blackcode.task_service.dto.TaskReq;
import com.blackcode.task_service.dto.TaskRes;
import com.blackcode.task_service.service.TaskService;
import com.blackcode.task_service.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/getAllTask")
    public ResponseEntity<ApiResponse<List<TaskRes>>> getAllTask() {
        List<TaskRes> taskRes = taskService.getAllTask();
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", 200, taskRes));
    }

    @GetMapping("/getTaskById/{id}")
    public ResponseEntity<ApiResponse<TaskRes>> getTaskById(@PathVariable("id") Long id){
        TaskRes taskRes = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task found",200, taskRes));
    }

    @PostMapping("/addTask")
    public ResponseEntity<ApiResponse<TaskRes>> addTask(@RequestBody TaskReq taskReq){
        TaskRes taskRes = taskService.addTask(taskReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Task created", 201, taskRes));
    }

    @PutMapping("/updateTask/{id}")
    public ResponseEntity<ApiResponse<TaskRes>> updateTask(@PathVariable("id") Long id, @RequestBody TaskReq taskReq){
        TaskRes taskRes = taskService.updateTask(id, taskReq);
        return ResponseEntity.ok(ApiResponse.success("Task Update", 200, taskRes));
    }

    @DeleteMapping("/deleteTask/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTask(@PathVariable("id") Long id){
        Map<String, Object> rtn = taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", 200, rtn));
    }
}
