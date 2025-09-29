package com.blackcode.user_service.controller;

import com.blackcode.user_service.dto.UserReq;
import com.blackcode.user_service.dto.UserRes;
import com.blackcode.user_service.dto.UserResSyn;
import com.blackcode.user_service.service.UserService;
import com.blackcode.user_service.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<ApiResponse<List<UserRes>>> getAllUser() {
        List<UserRes> userRes = userService.getAllUser();
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", 200, userRes));
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<ApiResponse<UserRes>> getUserById(@PathVariable("id") String id){
        UserRes userRes = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User found",200, userRes));
    }

    @PostMapping("/addUser")
    public ResponseEntity<ApiResponse<UserResSyn>> addUser(@RequestBody UserReq userReq){
        UserResSyn userRes = userService.addUser(userReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created", 201, userRes));
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<ApiResponse<UserRes>> updateUser(@PathVariable("id") String id, @RequestBody UserReq userReq){
        UserRes userRes = userService.updateUser(id, userReq);
        return ResponseEntity.ok(ApiResponse.success("User Update", 200, userRes));
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable("id") String id){
        Map<String, Object> rtn = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", 200, rtn));
    }
}
