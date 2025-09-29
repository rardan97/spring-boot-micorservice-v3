package com.blackcode.address_service.service;

import com.blackcode.address_service.dto.AddressReq;
import com.blackcode.address_service.dto.AddressRes;
import com.blackcode.address_service.exception.DataNotFoundException;
import com.blackcode.address_service.model.Address;
import com.blackcode.address_service.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceImpl implements AddressService{

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<AddressRes> getAllAddress() {
        List<Address> addressList = addressRepository.findAll();
        return addressList.stream().map(this::mapToAddressRes).toList();
    }

    @Override
    public AddressRes getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new DataNotFoundException("Address Not Found with id : "+addressId));

        System.out.println("address : "+address.getCity());
        return mapToAddressRes(address);

    }

    @Override
    public AddressRes addAddress(AddressReq addressReq) {
        Address address = new Address();
        address.setStreet(addressReq.getStreet());
        address.setCity(addressReq.getCity());
        address.setProvince(addressReq.getProvince());
        Address saveAddress = addressRepository.save(address);
        return mapToAddressRes(saveAddress);

    }

    @Override
    public AddressRes updateAddress(Long addressId, AddressReq addressReq) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new DataNotFoundException("Address Not Found with id : "+ addressId));

        address.setStreet(addressReq.getStreet());
        address.setCity(addressReq.getCity());
        address.setProvince(addressReq.getProvince());
        Address updateAddress = addressRepository.save(address);
        return mapToAddressRes(updateAddress);

    }

    @Override
    public Map<String, Object> deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new DataNotFoundException("Address Not Found with id :"+addressId));
        addressRepository.deleteById(addressId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deleteAddressId", addressId);
        responseData.put("info", "The Address was removed from the database");
        return responseData;

    }

    private AddressRes mapToAddressRes(Address address){
        AddressRes addressRes = new AddressRes();
        addressRes.setAddressId(address.getAddressId());
        addressRes.setStreet(address.getStreet());
        addressRes.setCity(address.getCity());
        addressRes.setProvince(address.getProvince());
        return addressRes;
    }
}
