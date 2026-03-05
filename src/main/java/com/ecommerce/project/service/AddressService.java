package com.ecommerce.project.service;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressDTO addAddress(AddressDTO addressDTO);

    AddressResponse getAllAddresses();

    AddressResponse getLoggedInUserAddresses();

    AddressDTO getAddressByAddressId(Long addressId);

    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);

    AddressDTO deleteAddress(Long addressId);
}
