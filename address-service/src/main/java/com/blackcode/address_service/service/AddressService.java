package com.blackcode.address_service.service;

import com.blackcode.address_service.dto.AddressReq;
import com.blackcode.address_service.dto.AddressRes;

import java.util.List;
import java.util.Map;

public interface AddressService {

    List<AddressRes> getAllAddress();

    AddressRes getAddressById(Long addressId);

    AddressRes addAddress(AddressReq addressReq);

    AddressRes updateAddress(Long addressId, AddressReq addressReq);

    Map<String, Object> deleteAddress(Long addressId);


}
