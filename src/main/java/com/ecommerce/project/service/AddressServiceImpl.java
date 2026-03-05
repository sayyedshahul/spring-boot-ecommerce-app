package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{
    private final AuthUtil authUtil;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Override
    public AddressDTO addAddress(AddressDTO addressDTO) {
        User user = authUtil.getLoggedInUser();
        Address address = modelMapper.map(addressDTO, Address.class);

        user.getAddresses().add(address);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address); // To get stored address id.

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public AddressResponse getAllAddresses() {
        List<Address> addressList = addressRepository.findAll();
        List<AddressDTO> addressDTOs = addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class)).toList();

        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setAddresses(addressDTOs);

        return addressResponse;
    }

    @Override
    public AddressResponse getLoggedInUserAddresses() {
        User user = authUtil.getLoggedInUser();

        List<Address> addresses = user.getAddresses();
        List<AddressDTO> addressDTOs = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class)).toList();

        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setAddresses(addressDTOs);

        return addressResponse;
    }

    @Override
    public AddressDTO getAddressByAddressId(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Address requestAddress = modelMapper.map(addressDTO, Address.class);

        address.setCity(requestAddress.getCity());
        address.setPincode(requestAddress.getPincode());
        address.setCountry(requestAddress.getCountry());
        address.setState(requestAddress.getState());
        address.setBuildingName(requestAddress.getBuildingName());

        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressRepository.delete(address);

        return modelMapper.map(address, AddressDTO.class);
    }
}
