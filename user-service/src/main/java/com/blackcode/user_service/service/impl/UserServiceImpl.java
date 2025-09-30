package com.blackcode.user_service.service.impl;

import com.blackcode.user_service.dto.*;
import com.blackcode.user_service.exception.DataNotFoundException;
import com.blackcode.user_service.helper.TypeRefs;
import com.blackcode.user_service.model.User;
import com.blackcode.user_service.repository.UserRepository;
import com.blackcode.user_service.service.AddressClientService;
import com.blackcode.user_service.service.DepartmentClientService;
import com.blackcode.user_service.service.UserService;
import com.blackcode.user_service.utils.ApiResponse;
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
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final DepartmentClientService departmentClientService;

    private final AddressClientService addressClientService;

    public UserServiceImpl(UserRepository userRepository,
                           DepartmentClientService departmentClientService,
                           AddressClientService addressClientService) {
        this.userRepository = userRepository;
        this.departmentClientService = departmentClientService;
        this.addressClientService = addressClientService;
    }

    @Override
    public List<UserRes> getAllUser() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(user -> {
            DepartmentDto department = departmentClientService.getDepartmentById(user.getDepartmentId());
            AddressDto address = addressClientService.getAddressById(user.getAddressId());
            return mapToUserRes(user, department, address);
        }).toList();
    }

    @Override
    public UserRes getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: "+userId));

        DepartmentDto department = departmentClientService.getDepartmentById(user.getDepartmentId());
        AddressDto address = addressClientService.getAddressById(user.getAddressId());
        return mapToUserRes(user, department, address);
    }

    @Override
    public UserResSyn addUser(UserReq userReq) {
        User user = new User();
        user.setUserId(userReq.getUserId());
        user.setNama(userReq.getNama());
        user.setEmail(userReq.getEmail());
        user.setDepartmentId(userReq.getDepartmentId());
        user.setAddressId(userReq.getAddressId());
        User saveUser = userRepository.save(user);

        return mapToUserResSyn(saveUser);
    }

    @Override
    public UserRes updateUser(String userId, UserReq userReq) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: "+userId));

        user.setNama(userReq.getNama());
        user.setEmail(userReq.getEmail());
        user.setDepartmentId(userReq.getDepartmentId());
        user.setAddressId(userReq.getAddressId());

        User updateUser = userRepository.save(user);
        DepartmentDto department = departmentClientService.getDepartmentById(updateUser.getDepartmentId());
        AddressDto address = addressClientService.getAddressById(updateUser.getAddressId());
        return mapToUserRes(updateUser, department, address);
    }

    @Override
    public Map<String, Object> deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: "+userId));
        userRepository.deleteById(userId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deletedUserId", userId);
        responseData.put("info", "The User was removed from the database.");
        return responseData;
    }

    private UserRes mapToUserRes(User user, DepartmentDto departmentDto, AddressDto addressDto){
        UserRes userRes = new UserRes();
        userRes.setUserId(user.getUserId());
        userRes.setNama(user.getNama());
        userRes.setEmail(user.getEmail());
        userRes.setAddress(addressDto);
        userRes.setDepartment(departmentDto);
        return userRes;
    }

    private UserResSyn mapToUserResSyn(User user){
        UserResSyn userRes = new UserResSyn();
        userRes.setUserId(user.getUserId());
        userRes.setNama(user.getNama());
        userRes.setEmail(user.getEmail());
        userRes.setAddress(user.getAddressId().toString());
        userRes.setDepartment(user.getDepartmentId().toString());
        return userRes;
    }


}
