package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtil;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OrderControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        public AuthUtil authUtil() {
            return Mockito.mock(AuthUtil.class);
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

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void placeOrder_shouldReturnCreatedOrder() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();

        OrderDTO response = new OrderDTO();
        response.setOrderId(1L);

        when(authUtil.getLoggedInEmail()).thenReturn("test@mail.com");
        when(orderService.placeOrder(Mockito.anyString(), Mockito.any(OrderRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void getAllOrders_shouldReturnOrderResponse() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);

        OrderResponse response = new OrderResponse();
        response.setContent(List.of(orderDTO));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(orderService.getAllOrders(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "orderDate")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));;
    }

    @Test
    void updateOrderStatus_shouldReturnUpdatedOrder() throws Exception {
        OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
        request.setOrderStatusUpdate("SHIPPED");

        OrderDTO response = new OrderDTO();
        response.setOrderId(1L);
        response.setOrderStatus("SHIPPED");

        when(orderService.updateOrderStatus(Mockito.eq(1L), Mockito.any(OrderStatusUpdateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));
    }

    @Test
    void getAllSellerOrders_shouldReturnSellerOrders() throws Exception {
        User seller = new User();

        SellerOrderResponseDTO response = new SellerOrderResponseDTO();

        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(1L);

        response.setContent(List.of(orderItemDTO));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(authUtil.getLoggedInUser()).thenReturn(seller);
        when(orderService.getAllSellerOrders(Mockito.eq(seller),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/seller/orders")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "quantity")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderItemId").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }
}