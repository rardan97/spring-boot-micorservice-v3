package com.blackcode.task_service.service.impl;

import com.blackcode.task_service.dto.TaskReq;
import com.blackcode.task_service.dto.TaskRes;
import com.blackcode.task_service.dto.UserDto;
import com.blackcode.task_service.exception.DataNotFoundException;
import com.blackcode.task_service.model.Task;
import com.blackcode.task_service.repository.TaskRepository;
import com.blackcode.task_service.service.TaskService;
import com.blackcode.task_service.service.UserClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    private final UserClientService userClientService;

    public TaskServiceImpl(TaskRepository taskRepository, UserClientService userClientService) {
        this.taskRepository = taskRepository;
        this.userClientService = userClientService;
    }

    @Override
    public List<TaskRes> getAllTask() {
        List<Task> userList = taskRepository.findAll();
        return userList.stream().map(task -> {
            UserDto userDto = userClientService.getUserById(task.getTaskUserId());
            return mapToTaskRes(task, userDto);
        }).toList();
    }

    @Override
    public TaskRes getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: "+taskId));

        UserDto userDto = userClientService.getUserById(task.getTaskUserId());
        return mapToTaskRes(task, userDto);
    }

    @Override
    public TaskRes addTask(TaskReq taskReq) {
        Task task = new Task();
        task.setTaskName(taskReq.getTaskName());
        task.setTaskDescription(taskReq.getTaskDescription());
        task.setTaskUserId(taskReq.getTaskUserId());
        Task saveTask = taskRepository.save(task);
        UserDto userDto = userClientService.getUserById(saveTask.getTaskUserId());
        return mapToTaskRes(saveTask, userDto);
    }

    @Override
    public TaskRes updateTask(Long taskId, TaskReq taskReq) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: "+taskId));

        task.setTaskName(taskReq.getTaskName());
        task.setTaskDescription(taskReq.getTaskDescription());

        Task updateTask = taskRepository.save(task);
        UserDto userDto = userClientService.getUserById(updateTask.getTaskUserId());
        return mapToTaskRes(updateTask, userDto);
    }

    @Override
    public Map<String, Object> deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: "+taskId));
        taskRepository.deleteById(taskId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deletedTaskId", taskId);
        responseData.put("info", "The Task was removed from the database.");
        return responseData;
    }

    private TaskRes mapToTaskRes(Task task, UserDto userDto){
        TaskRes taskRes = new TaskRes();
        taskRes.setTaskId(task.getTaskId());
        taskRes.setTaskName(task.getTaskName());
        taskRes.setTaskDescription(task.getTaskDescription());
        taskRes.setTaskUser(userDto);
        return taskRes;
    }
}
