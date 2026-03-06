package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.*;

public interface OrderService {
    OrderDTO placeOrder(String email, OrderRequestDTO orderRequestDTO);

    OrderResponse getAllOrders(int pageNumber, int pageSize, String sortBy, String sortOrder);

    OrderDTO updateOrderStatus(Long orderId, OrderStatusUpdateDTO orderStatusUpdateDTO);

    SellerOrderResponseDTO getAllSellerOrders(User seller, int pageNumber, int pageSize, String sortBy, String sortOrder);
}
