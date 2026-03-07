package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address API")
public class AddressController {
    private final AddressService addressService;

    @Operation(summary = "Add a new address")
    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(@RequestBody @Valid AddressDTO addressDTO){
        AddressDTO savedAddressDTO = addressService.addAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Get All addresses")
    @GetMapping
    public ResponseEntity<AddressResponse> getAllAddresses(){
        AddressResponse allAddresses = addressService.getAllAddresses();
        return new ResponseEntity<>(allAddresses, HttpStatus.OK);
    }

    @Operation(summary = "Get logged in user address")
    @GetMapping("/users/addresses")
    public ResponseEntity<AddressResponse> getLoggedInUserAddresses(){
        AddressResponse addressResponse = addressService.getLoggedInUserAddresses();
        return new ResponseEntity<>(addressResponse, HttpStatus.OK);
    }

    @Operation(summary = "Get address by address id")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressByAddressId(@PathVariable Long addressId){
        AddressDTO addressDTO = addressService.getAddressByAddressId(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @Operation(summary = "Update address by address id")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @Operation(summary = "Delete address by address id")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddress(@PathVariable Long addressId){
        AddressDTO deletedAddressDTO = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(deletedAddressDTO, HttpStatus.OK);
    }
}
