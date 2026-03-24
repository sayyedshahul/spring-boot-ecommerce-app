package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRepository orderRepository;

    private ModelMapper modelMapper = new ModelMapper();
    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        orderService = new OrderServiceImpl(cartRepository, addressRepository, orderItemRepository, orderRepository, modelMapper);
    }

    // ------------------ Helpers ------------------

    private Cart getCartWithItems() {
        Product product = new Product();
        product.setProductId(1L);
        product.setQuantity(10);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductPrice(100.0);
        item.setDiscount(10.0);

        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setTotalPrice(200.0);
        cart.setCartItems(new ArrayList<>(List.of(item)));

        return cart;
    }

    private OrderRequestDTO getOrderRequest() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setAddressId(1L);
        dto.setPaymentMethod("CARD");
        dto.setPgName("razorpay");
        dto.setPgStatus("SUCCESS");
        dto.setPgPaymentId("pi_1FHEhK2eZvKYlo2CcK4UJNdW");
        dto.setPgResponseMessage("OK");
        return dto;
    }

    private List<Order> getOrderList(){
        Order order = new Order();
        Payment payment = new Payment();

        Address address = new Address();
        address.setAddressId(1L);

        order.setPayment(payment);
        order.setAddress(address);

        OrderItem orderItem = new OrderItem();
        Product product = new Product();
        orderItem.setProduct(product);

        order.getOrderItems().add(orderItem);

        return new ArrayList<>(List.of(order));
    }

    // ------------------ placeOrder ------------------

    @Test
    void placeOrder_shouldPlaceSuccessfully() {
        String email = "test@test.com";

        Cart cart = getCartWithItems();
        Address address = new Address();
        address.setAddressId(1L);

        Product product = cart.getCartItems().get(0).getProduct();

        when(cartRepository.findCartByUserEmail(email)).thenReturn(cart);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(orderItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO result = orderService.placeOrder(email, getOrderRequest());

        verify(orderItemRepository).saveAll(anyList());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(200.0, savedOrder.getTotalAmount());

        assertNotNull(result);
        assertEquals(0.0, cart.getTotalPrice()); // cart cleared
        assertTrue(cart.getCartItems().isEmpty());

        assertEquals(200.0, result.getTotalAmount());
        assertEquals("CARD", result.getPaymentDTO().getPaymentMethod());
        assertEquals(1L, result.getAddressId());

        assertEquals(1, result.getOrderItems().size());
        assertEquals(2, result.getOrderItems().get(0).getQuantity());
        assertEquals(100.0, result.getOrderItems().get(0).getOrderedProductPrice());

        assertEquals(8, product.getQuantity()); // Assert new stock quantity.
    }

    @Test
    void placeOrder_shouldThrowException_whenCartNotFound() {
        when(cartRepository.findCartByUserEmail(anyString()))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@test.com", getOrderRequest()));
    }

    @Test
    void placeOrder_shouldThrowException_whenAddressNotFound() {
        when(cartRepository.findCartByUserEmail(anyString()))
                .thenReturn(getCartWithItems());
        when(addressRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@test.com", getOrderRequest()));
    }

    @Test
    void placeOrder_shouldThrowException_whenCartIsEmpty() {
        Cart cart = new Cart();
        cart.setCartItems(List.of());

        when(cartRepository.findCartByUserEmail(anyString()))
                .thenReturn(cart);
        when(addressRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Address()));

        assertThrows(APIException.class,
                () -> orderService.placeOrder("test@test.com", getOrderRequest()));
    }

    // ------------------ getAllOrders ------------------

    @Test
    void getAllOrders_shouldReturnPagedResponse() {
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "totalAmount";
        String sortOrder = "asc";

        List<Order> orders = getOrderList();

        Page<Order> page = new PageImpl<>(orders,
                PageRequest.of(pageNumber, pageSize), 1);

        when(orderRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        OrderResponse response =
                orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(orderRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        Assertions.assertEquals(pageNumber, pageable.getPageNumber());
        Assertions.assertEquals(pageSize, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(Sort.Direction.ASC, order.getDirection());

        Assertions.assertEquals(1, response.getContent().size());
        Assertions.assertEquals(pageNumber, response.getPageNumber());
        Assertions.assertEquals(pageSize, response.getPageSize());
        Assertions.assertEquals(1, response.getTotalElements());
        Assertions.assertEquals(1 ,response.getTotalPages());
        Assertions.assertTrue(response.isLastPage());
    }

    // ------------------ updateOrderStatus ------------------

    @Test
    void updateOrderStatus_shouldUpdateSuccessfully() {
        Order order = getOrderList().get(0);
        order.setOrderId(1L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO();
        dto.setOrderStatusUpdate("SHIPPED");

        OrderDTO result = orderService.updateOrderStatus(1L, dto);

        verify(orderRepository).save(order);

        assertEquals("SHIPPED", order.getOrderStatus());
        assertEquals("SHIPPED", result.getOrderStatus());
    }

    @Test
    void updateOrderStatus_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(1L, new OrderStatusUpdateDTO()));
    }

    // ------------------ getAllSellerOrders ------------------

    @Test
    void getAllSellerOrders_shouldReturnData() {
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "orderedProductPrice";
        String sortOrder = "asc";

        User seller = new User();
        seller.setEmail("seller@test.com");

        OrderItem orderItem = new OrderItem();
        Product product = new Product();
        orderItem.setProduct(product);

        List<OrderItem> sellerOrderItems = List.of(orderItem);

        Page<OrderItem> page = new PageImpl<>(sellerOrderItems,
                PageRequest.of(pageNumber, pageSize), 1);

        when(orderItemRepository.findOrderItemsBySellerEmail(eq(seller.getEmail()), any(Pageable.class)))
                .thenReturn(page);

        SellerOrderResponseDTO response =
                orderService.getAllSellerOrders(seller, 0, 10, sortBy, sortOrder);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(orderItemRepository).findOrderItemsBySellerEmail(eq(seller.getEmail()), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        Assertions.assertEquals(pageNumber, pageable.getPageNumber());
        Assertions.assertEquals(pageSize, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(Sort.Direction.ASC, order.getDirection());

        Assertions.assertEquals(1, response.getContent().size());
        Assertions.assertEquals(pageNumber, response.getPageNumber());
        Assertions.assertEquals(pageSize, response.getPageSize());
        Assertions.assertEquals(1, response.getTotalElements());
        Assertions.assertEquals(1 ,response.getTotalPages());
        Assertions.assertTrue(response.isLastPage());
    }

    // -----------------mapOrderToOrderDTO----------
    @Test
    void mapOrderToOrderDTO(){
        Order order = getOrderList().get(0);

        OrderDTO orderDTO = orderService.mapOrderToOrderDTO(order);

        assertEquals(1, orderDTO.getOrderItems().size());
        assertNotNull(orderDTO.getOrderItems().get(0).getProductDTO());
        assertNotNull(orderDTO.getPaymentDTO());
        assertEquals(1L, orderDTO.getAddressId());
    }
}