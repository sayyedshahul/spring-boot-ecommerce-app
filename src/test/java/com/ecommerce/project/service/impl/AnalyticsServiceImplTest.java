package com.ecommerce.project.service.impl;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void getAnalyticsData_shouldReturnAnalyticsResponse(){
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.getTotalRevenue()).thenReturn(23000.0);
        when(productRepository.count()).thenReturn(35L);

        AnalyticsResponse response =analyticsService.getAnalyticsData();

        assertEquals(100L, response.getTotalOrders());
        assertEquals(23000.0, response.getTotalRevenue());
        assertEquals(35L, response.getProductCount());
    }
}
