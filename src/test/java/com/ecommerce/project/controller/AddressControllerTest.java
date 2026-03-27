package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AddressControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AddressService addressService() {
            return Mockito.mock(AddressService.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }

        @Bean
        public AuthTokenFilter authTokenFilter() {
            return Mockito.mock(AuthTokenFilter.class);
        }

        @Bean
        public UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Test
    void addAddress_shouldReturnCreated() throws Exception {
        AddressDTO request = new AddressDTO();
        AddressDTO response = new AddressDTO();

        request.setCity("City1");
        request.setCountry("India");
        request.setPincode("12345");
        request.setBuildingName("buildingname");
        request.setState("state");

        response.setCity("City1");
        response.setCountry("India");
        response.setPincode("12345");
        response.setBuildingName("buildingname");
        response.setState("state");
        response.setAddressId(1L);

        when(addressService.addAddress(any(AddressDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pincode").value("12345"))
                .andExpect(jsonPath("$.addressId").isNotEmpty());
    }

    @Test
    void getAllAddresses_shouldReturnOk() throws Exception {
        AddressResponse response = new AddressResponse();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setPincode("12345");

        response.setAddresses(List.of(addressDTO));

        when(addressService.getAllAddresses()).thenReturn(response);

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses.length()").value(1))
                .andExpect(jsonPath("$.addresses[0].pincode").value("12345"));
    }

    @Test
    void getLoggedInUserAddresses_shouldReturnOk() throws Exception {
        AddressResponse response = new AddressResponse();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setPincode("12345");
        response.setAddresses(List.of(addressDTO));

        when(addressService.getLoggedInUserAddresses()).thenReturn(response);

        mockMvc.perform(get("/api/addresses/users/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses[0].pincode").value("12345"));
    }

    @Test
    void getAddressById_shouldReturnOk() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(1L);

        when(addressService.getAddressByAddressId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1));
    }

    @Test
    void updateAddress_shouldReturnOk() throws Exception {
        AddressDTO request = new AddressDTO();
        request.setCity("Nagpur");

        AddressDTO response = new AddressDTO();
        response.setCity("Nagpur");

        when(addressService.updateAddress(Mockito.eq(1L), any(AddressDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Nagpur"));
    }

    @Test
    void deleteAddress_shouldReturnOk() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(1L);

        when(addressService.deleteAddress(1L)).thenReturn(dto);

        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1L));
    }
}