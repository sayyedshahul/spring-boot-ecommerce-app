package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.service.AnalyticsService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AnalyticsControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AnalyticsService analyticsService() {
            return Mockito.mock(AnalyticsService.class);
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
    void getAnalyticsData_shouldReturnAnalyticsResponse() throws Exception{
        AnalyticsResponse analyticsResponse = new AnalyticsResponse();
        analyticsResponse.setTotalOrders(2312L);

        when(analyticsService.getAnalyticsData()).thenReturn(analyticsResponse);

        mockMvc.perform(get("/api/admin/app/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(2312L));
    }
}
