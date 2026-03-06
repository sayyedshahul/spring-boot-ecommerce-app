package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public OrderDTO placeOrder(String email, OrderRequestDTO orderRequestDTO) {
        Cart cart = cartRepository.findCartByUserEmail(email);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        Address address = addressRepository.findById(orderRequestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", orderRequestDTO.getAddressId()));

        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);
        order.setEmail(email);
        order.setTotalAmount(cart.getTotalPrice());

        Payment payment = new Payment(orderRequestDTO.getPaymentMethod(),
                orderRequestDTO.getPgStatus(), orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgResponseMessage(), orderRequestDTO.getPgName());
        order.setPayment(payment);
        payment.setOrder(order);
        orderRepository.save(order);

        //paymentRepository.save(payment);

        // Convert cart items to order items.
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new APIException("No items present in the user cart");
        }

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            return orderItem;
        }).toList();
        orderItems = orderItemRepository.saveAll(orderItems);

        order.setOrderItems(orderItems);

        for(CartItem cartItem: cart.getCartItems()){
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            cartItem.setCart(null);
        }
        cart.getCartItems().clear();
        cart.setTotalPrice(0.0);

        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(orderItem ->{
                OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
                orderItemDTO.setProductDTO(modelMapper.map(orderItem.getProduct(), ProductDTO.class));
                return orderItemDTO;
                })
                .toList();
        PaymentDTO paymentDTO = modelMapper.map(payment, PaymentDTO.class);

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        orderDTO.setOrderItems(orderItemDTOs);
        orderDTO.setAddressId(address.getAddressId());
        orderDTO.setPaymentDTO(paymentDTO);

        return orderDTO;
    }

    @Override
    public OrderResponse getAllOrders(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> orderPage = orderRepository.findAll(pageDetails);
        List<Order> orders = orderPage.getContent();

        List<OrderDTO> orderDTOs = orders.stream().map(this::mapOrderToOrderDTO).toList();

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(orderPage.getNumber());
        orderResponse.setLastPage(orderPage.isLast());
        orderResponse.setTotalPages(orderPage.getTotalPages());
        orderResponse.setPageSize(orderPage.getSize());
        orderResponse.setTotalElements(orderPage.getTotalElements());

        return orderResponse;
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatusUpdateDTO orderStatusUpdateDTO) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        order.setOrderStatus(orderStatusUpdateDTO.getOrderStatusUpdate());
        orderRepository.save(order);

        return mapOrderToOrderDTO(order);
    }

    @Override
    public SellerOrderResponseDTO getAllSellerOrders(User seller, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<OrderItem> sellerOrderItemsPage = orderItemRepository.findOrdersBySellerEmail(seller.getEmail(), pageDetails);
        List<OrderItem> sellerOrderItems = sellerOrderItemsPage.getContent();


        List<OrderItemDTO> sellerOrderItemDTOs = sellerOrderItems.stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
            orderItemDTO.setProductDTO(modelMapper.map(orderItem.getProduct(), ProductDTO.class));
            return orderItemDTO;
        }).toList();

        SellerOrderResponseDTO response = new SellerOrderResponseDTO();
        response.setSellerOrderItems(sellerOrderItemDTOs);

        return response;
    }

    public OrderDTO mapOrderToOrderDTO(Order order){

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem ->{
                    OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
                    orderItemDTO.setProductDTO(modelMapper.map(orderItem.getProduct(), ProductDTO.class));
                    return orderItemDTO;
                })
                .toList();

        orderDTO.setOrderItems(orderItemDTOs);

        PaymentDTO paymentDTO = modelMapper.map(order.getPayment(), PaymentDTO.class);

        orderDTO.setPaymentDTO(paymentDTO);
        orderDTO.setAddressId(order.getAddress().getAddressId());

        return orderDTO;
    }
}
