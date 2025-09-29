package com.blackcode.task_service.service;

import com.blackcode.task_service.dto.TaskReq;
import com.blackcode.task_service.dto.TaskRes;
import com.blackcode.task_service.dto.UserDto;
import com.blackcode.task_service.exception.DataNotFoundException;
import com.blackcode.task_service.helper.TypeRefs;
import com.blackcode.task_service.model.Task;
import com.blackcode.task_service.repository.TaskRepository;
import com.blackcode.task_service.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private static final String USER_API_PATH = "/api/user/getUserById/";

    private final TaskRepository taskRepository;

    private final WebClient userClient;

    public TaskServiceImpl(TaskRepository taskRepository,
                           @Qualifier("userClient") WebClient userClient) {
        this.taskRepository = taskRepository;
        this.userClient = userClient;
    }

    @Override
    public List<TaskRes> getAllTask() {
        List<Task> userList = taskRepository.findAll();
        return userList.stream().map(task -> {
            UserDto userDto = fetchUserById(task.getTaskUserId());
            return mapToTaskRes(task, userDto);
        }).toList();
    }

    @Override
    public TaskRes getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: "+taskId));

        UserDto userDto = fetchUserById(task.getTaskUserId());
        return mapToTaskRes(task, userDto);
    }

    @Override
    public TaskRes addTask(TaskReq taskReq) {
        Task task = new Task();
        task.setTaskName(taskReq.getTaskName());
        task.setTaskDescription(taskReq.getTaskDescription());
        task.setTaskUserId(taskReq.getTaskUserId());
        Task saveTask = taskRepository.save(task);
        UserDto userDto = fetchUserById(saveTask.getTaskUserId());
        return mapToTaskRes(saveTask, userDto);
    }

    @Override
    public TaskRes updateTask(Long taskId, TaskReq taskReq) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: "+taskId));

        task.setTaskName(taskReq.getTaskName());
        task.setTaskDescription(taskReq.getTaskDescription());

        Task updateTask = taskRepository.save(task);
        UserDto userDto = fetchUserById(updateTask.getTaskUserId());
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

    private UserDto fetchUserById(String userId){
        System.out.println("userId fetch :"+userId);
        if(userId == null){
            return null;
        }
        String uri = USER_API_PATH+userId;
        return fetchExternalData(
                userClient,
                uri,
                TypeRefs.userDtoResponse(),
                userId,
                "User"
        );
    }

    private <T> T fetchExternalData(WebClient client, String uri, ParameterizedTypeReference<ApiResponse<T>> typeRef, String logId, String dataType){
        try {
            ApiResponse<T> response = client.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            clientResponse -> Mono.error(new DataNotFoundException(dataType +" not found"))
                    )
                    .bodyToMono(typeRef)
                    .timeout(Duration.ofSeconds(3))
                    .onErrorResume(e -> {
                        logger.warn("{} not found for ID {}: {}", dataType, logId, e.getMessage());
                        return Mono.error(e);
                    })
                    .block();
            if (response == null) {
                logger.warn("No response received for {} ID {}", dataType, logId);
                return null;
            }
            return response.getData();

        }catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof java.util.concurrent.TimeoutException) {
                logger.error("Timeout fetching {} {}: {}", dataType, logId, e.getMessage());
                return null;
            }
            throw e;
        }catch (Exception e){
            logger.error("Unexpected error fetching {} {}: {}", dataType, logId, e.getMessage());
            return null;
        }
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
