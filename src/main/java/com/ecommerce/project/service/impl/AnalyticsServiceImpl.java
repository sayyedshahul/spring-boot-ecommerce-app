package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService{
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public AnalyticsResponse getAnalyticsData() {
        AnalyticsResponse analyticsResponse = new AnalyticsResponse();

        analyticsResponse.setProductCount(productRepository.count());
        analyticsResponse.setTotalOrders(orderRepository.count());
        analyticsResponse.setTotalRevenue(orderRepository.getTotalRevenue());

        return analyticsResponse;
    }
}
