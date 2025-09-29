package com.blackcode.user_service.service;

import com.blackcode.user_service.dto.*;
import com.blackcode.user_service.exception.DataNotFoundException;
import com.blackcode.user_service.helper.TypeRefs;
import com.blackcode.user_service.model.User;
import com.blackcode.user_service.repository.UserRepository;
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
public class UserServiceImpl implements UserService{

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String DEPARTMENT_API_PATH = "/api/department/getDepartmentById/";

    private static final String ADDRESS_API_PATH = "/api/address/getAddressById/";

    private final UserRepository userRepository;

    private final WebClient departmentClient;

    private final WebClient addressClient;

    public UserServiceImpl(UserRepository userRepository,
                           @Qualifier("departmentClient") WebClient departmentClient,
                           @Qualifier("addressClient") WebClient addressClient) {
        this.userRepository = userRepository;
        this.departmentClient = departmentClient;
        this.addressClient = addressClient;
    }


    @Override
    public List<UserRes> getAllUser() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(user -> {
            DepartmentDto department = fetchDepartmentById(user.getDepartmentId());
            AddressDto address = fetchAddressById(user.getAddressId());
            return mapToUserRes(user, department, address);
        }).toList();
    }

    @Override
    public UserRes getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: "+userId));

        DepartmentDto department = fetchDepartmentById(user.getDepartmentId());
        AddressDto address = fetchAddressById(user.getAddressId());
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
        DepartmentDto department = fetchDepartmentById(updateUser.getDepartmentId());
        AddressDto address = fetchAddressById(updateUser.getAddressId());
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


    private DepartmentDto fetchDepartmentById(Long departmentId){
        System.out.println("department fetch :"+departmentId);
        if(departmentId == null){
            return null;
        }
        String uri = DEPARTMENT_API_PATH+departmentId;
        return fetchExternalData(
                departmentClient,
                uri,
                TypeRefs.departmentDtoResponse(),
                departmentId.toString(),
                "Department"
        );
    }

    private AddressDto fetchAddressById(Long addressId){
        System.out.println("address fetch :"+addressId);
        if(addressId == null){
            return null;
        }
        String uri = ADDRESS_API_PATH + addressId;
        System.out.println("address fetch :"+uri);
        return fetchExternalData(
                addressClient,
                uri,
                TypeRefs.addressDtoResponse(),
                addressId.toString(),
                "Address"
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
