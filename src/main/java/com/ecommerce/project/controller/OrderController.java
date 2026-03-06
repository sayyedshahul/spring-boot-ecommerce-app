package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final AuthUtil authUtil;
    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderRequestDTO orderRequestDTO){
        String email = authUtil.getLoggedInEmail();
        OrderDTO savedOrder = orderService.placeOrder(email, orderRequestDTO);
        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderResponse> getAllOrders(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
                                                      @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
                                                      @RequestParam(defaultValue = AppConstants.SORT_ORDERS_BY) String sortBy,
                                                      @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO){
        OrderDTO orderDTO = orderService.updateOrderStatus(orderId, orderStatusUpdateDTO);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @GetMapping("/seller/orders")
    public ResponseEntity<SellerOrderResponseDTO> getAllSellerOrders(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
                                                                     @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
                                                                     @RequestParam(defaultValue = AppConstants.SORT_SELLER_ORDERS_BY) String sortBy,
                                                                     @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        User seller = authUtil.getLoggedInUser();
        SellerOrderResponseDTO response = orderService.getAllSellerOrders(seller, pageNumber, pageSize, sortBy, sortOrder) ;
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
