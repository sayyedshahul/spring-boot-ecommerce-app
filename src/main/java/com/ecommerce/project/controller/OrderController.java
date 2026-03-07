package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Order API")
public class OrderController {
    private final AuthUtil authUtil;
    private final OrderService orderService;

    @Operation(summary = "Place a new order")
    @PostMapping("/order")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderRequestDTO orderRequestDTO){
        String email = authUtil.getLoggedInEmail();
        OrderDTO savedOrder = orderService.placeOrder(email, orderRequestDTO);
        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "Get All orders")
    @GetMapping("/orders")
    public ResponseEntity<OrderResponse> getAllOrders(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
                                                      @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
                                                      @RequestParam(defaultValue = AppConstants.SORT_ORDERS_BY) String sortBy,
                                                      @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @Operation(summary = "Update order status by order id")
    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO){
        OrderDTO orderDTO = orderService.updateOrderStatus(orderId, orderStatusUpdateDTO);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @Operation(summary = "Get all orders for the currently logged in seller")
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
