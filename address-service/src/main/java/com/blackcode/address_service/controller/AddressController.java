package com.blackcode.address_service.controller;

import com.blackcode.address_service.dto.AddressReq;
import com.blackcode.address_service.dto.AddressRes;
import com.blackcode.address_service.service.AddressService;
import com.blackcode.address_service.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/getAllAddress")
    public ResponseEntity<ApiResponse<List<AddressRes>>> getAllAddress() {
        List<AddressRes> addressRes = addressService.getAllAddress();
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", 200, addressRes));
    }

    @GetMapping("/getAddressById/{id}")
    public ResponseEntity<ApiResponse<AddressRes>> getAddressById(@PathVariable("id") Long id){
        AddressRes addressRes = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Address found",200, addressRes));
    }

    @PostMapping("/addAddress")
    public ResponseEntity<ApiResponse<AddressRes>> addAddress(@RequestBody AddressReq addressReq){
        AddressRes addressRes = addressService.addAddress(addressReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address created", 201, addressRes));
    }

    @PutMapping("/updateAddress/{id}")
    public ResponseEntity<ApiResponse<AddressRes>> updateAddress(@PathVariable("id") Long id, @RequestBody AddressReq addressReq){
        AddressRes addressRes = addressService.updateAddress(id, addressReq);
        return ResponseEntity.ok(ApiResponse.success("Address Update", 200, addressRes));
    }

    @DeleteMapping("/deleteAddress/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAddress(@PathVariable("id") Long id){
        Map<String, Object> rtn = addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", 200, rtn));
    }



}
