package com.ecommerce.project.controller;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(@RequestBody @Valid AddressDTO addressDTO){
        AddressDTO savedAddressDTO = addressService.addAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<AddressResponse> getAllAddresses(){
        AddressResponse allAddresses = addressService.getAllAddresses();
        return new ResponseEntity<>(allAddresses, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<AddressResponse> getLoggedInUserAddresses(){
        AddressResponse addressResponse = addressService.getLoggedInUserAddresses();
        return new ResponseEntity<>(addressResponse, HttpStatus.OK);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressByAddressId(@PathVariable Long addressId){
        AddressDTO addressDTO = addressService.getAddressByAddressId(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddress(@PathVariable Long addressId){
        AddressDTO deletedAddressDTO = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(deletedAddressDTO, HttpStatus.OK);
    }
}
