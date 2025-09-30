package com.blackcode.user_service.service;

import com.blackcode.user_service.dto.AddressDto;
import com.blackcode.user_service.dto.DepartmentDto;

public interface AddressClientService {

    AddressDto getAddressById(Long addressId);
}
