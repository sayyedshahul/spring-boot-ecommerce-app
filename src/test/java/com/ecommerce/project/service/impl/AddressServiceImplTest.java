package com.ecommerce.project.service.impl;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {

    private AddressServiceImpl addressService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private AddressRepository addressRepository;

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setup() {
        addressService = new AddressServiceImpl(authUtil, addressRepository, modelMapper);
    }

    @Test
    void addAddress_shouldAddAndReturnDTO() {
        User user = new User();

        AddressDTO dto = new AddressDTO();
        Address savedAddress = new Address();

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

        AddressDTO result = addressService.addAddress(dto);

        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        Mockito.verify(addressRepository).save(addressCaptor.capture());
        Address capturedAddress = addressCaptor.getValue();

        assertNotNull(result);
        assertEquals(1, user.getAddresses().size());
        assertEquals(user, capturedAddress.getUser());
    }

    @Test
    void getAllAddresses_shouldReturnList() {
        Address address = new Address();
        List<Address> addresses = List.of(address);

        when(addressRepository.findAll()).thenReturn(addresses);
        AddressResponse response = addressService.getAllAddresses();

        assertNotNull(response);
        assertEquals(1, response.getAddresses().size());
    }

    @Test
    void getLoggedInUserAddresses_shouldReturnUserAddresses() {
        User user = new User();
        Address address = new Address();
        user.setAddresses(List.of(address));

        when(authUtil.getLoggedInUser()).thenReturn(user);

        AddressResponse response = addressService.getLoggedInUserAddresses();

        assertNotNull(response);
        assertEquals(1, response.getAddresses().size());
    }

    @Test
    void getAddressByAddressId_shouldReturnDTO() {
        Address address = new Address();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        AddressDTO result = addressService.getAddressByAddressId(1L);

        assertNotNull(result);
    }

    @Test
    void getAddressByAddressId_shouldThrowException_WhenAddressNotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.getAddressByAddressId(1L));
    }

    @Test
    void updateAddress_shouldUpdateAndReturnDTO() {
        Address existing = new Address();

        AddressDTO inputDto = new AddressDTO();
        inputDto.setCity("city");
        inputDto.setState("state");
        inputDto.setCountry("country");
        inputDto.setPincode("123");
        inputDto.setBuildingName("building");

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(existing)).thenReturn(existing);

        AddressDTO result = addressService.updateAddress(1L, inputDto);

        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        Mockito.verify(addressRepository).save(addressCaptor.capture());
        Address capturedAddress = addressCaptor.getValue();

        assertEquals("123", capturedAddress.getPincode());

        assertNotNull(result);
        assertEquals("city", result.getCity());
        assertEquals("state", result.getState());
        assertEquals("country", result.getCountry());
        assertEquals("123", result.getPincode());
        assertEquals("building", result.getBuildingName());
    }

    @Test
    void updateAddress_shouldThrowException_WhenAddressNotFound() {
        AddressDTO inputDto = new AddressDTO();

        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.updateAddress(1L, inputDto));
    }

    @Test
    void deleteAddress_shouldDeleteAndReturnDTO() {
        Address address = new Address();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        AddressDTO result = addressService.deleteAddress(1L);

        verify(addressRepository).delete(address);
        assertNotNull(result);
    }

    @Test
    void deleteAddress_shouldThrowException_WhenAddressNotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.deleteAddress(1L));
    }
}