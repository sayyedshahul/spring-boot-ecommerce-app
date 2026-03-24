package com.ecommerce.project.service.impl;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private AuthUtil authUtil;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    private CartServiceImpl cartService;

    @BeforeEach
    void setup() {
        cartService = new CartServiceImpl(authUtil, cartRepository, cartItemRepository, productRepository, modelMapper);
    }

    // -------------------- Helpers --------------------

    private User getUser() {
        User user = new User();
        user.setUserId(1L);
        return user;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Laptop");
        product.setQuantity(10);
        product.setDiscount(10.0);
        product.setSpecialPrice(100.0);
        return product;
    }

    private Cart getCart(User user) {
        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setUser(user);
        cart.setTotalPrice(0.0);
        cart.setCartItems(new ArrayList<>());
        return cart;
    }

    // -------------------- addProductToCart --------------------

    @Test
    void addProductToCart_shouldAddSuccessfully() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(null);

        CartDTO result = cartService.addProductToCart(1L, 2);

        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository).save(cart);

        assertEquals(200.0, cart.getTotalPrice());
        assertEquals(1, result.getProductDTOs().size());
    }

    @Test
    void addProductToCart_shouldThrowException_whenProductNotFound() {
        User user = getUser();
        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addProductToCart(1L, 5));
    }

    @Test
    void addProductToCart_shouldThrowException_whenCartItemAlreadyExists() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(new CartItem());

        assertThrows(APIException.class,
                () -> cartService.addProductToCart(1L, 1));
    }

    @Test
    void addProductToCart_shouldThrowException_whenOutOfStock() {
        User user = getUser();
        Product product = getProduct();
        product.setQuantity(0);

        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(APIException.class,
                () -> cartService.addProductToCart(1L, 1));
    }

    @Test
    void addProductToCart_shouldThrowException_whenStockIsLessThanCartItemQuantity() {
        User user = getUser();
        Product product = getProduct();
        product.setQuantity(3);

        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(APIException.class,
                () -> cartService.addProductToCart(1L, 5));
    }

    // ------------------Get All Carts ---------------------------
    @Test
    void shouldReturnAllCarts(){
        User user = getUser();
        Cart cart = getCart(user);
        Product product = getProduct();

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cart.getCartItems().add(cartItem);

        List<Cart> carts = List.of(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        List<CartDTO> cartDTOs = cartService.getAllCarts();

        verify(cartRepository).findAll();
        assertEquals(1, cartDTOs.size());
        assertEquals(1, cartDTOs.get(0).getProductDTOs().size());
    }

    // -------------------- getCart --------------------

    @Test
    void getCart_shouldReturnCartDTO() {
        Cart cart = getCart(getUser());

        when(cartRepository.findCartByUserEmailAndCartId("test@test.com", 1L))
                .thenReturn(Optional.of(cart));

        CartDTO result = cartService.getCart("test@test.com", 1L);

        assertNotNull(result);
    }

    @Test
    void getCart_shouldThrow_whenNotFound() {
        when(cartRepository.findCartByUserEmailAndCartId("test@test.com", 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCart("test@test.com", 1L));
    }

    //---------------mapCartToCartDTO--------------------
    @Test
    void shouldMapCartToCartDTO(){
        Product product = getProduct();

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = getCart(getUser());
        cart.setTotalPrice(200.0);
        cart.setCartItems(List.of(item));

        CartDTO result = cartService.mapCartToCartDTO(cart);

        assertNotNull(result);
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getProductDTOs().size());

        ProductDTO productDTO = result.getProductDTOs().get(0);

        assertEquals(1L, productDTO.getProductId());
        assertEquals(2, productDTO.getQuantity());
    }

    // -------------------- updateProductQuantity --------------------

    @Test
    void updateProductQuantity_shouldIncreaseQuantity() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductPrice(100.0);

        cart.setCartItems(List.of(item));
        cart.setTotalPrice(200.0);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(item);

        CartDTO result = cartService.updateProductQuantity(1L, 1, user);

        assertNotNull(result);
        assertEquals(300.0, cart.getTotalPrice());
    }

    @Test
    void updateProductQuantity_shouldRemoveCartItem_whenQuantityZero() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductPrice(100.0);

        cart.setTotalPrice(200.0);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(item);

        CartDTO resultDTO = cartService.updateProductQuantity(1L, -2, user);

        verify(cartItemRepository)
                .deleteCartItemByCartIdAndProductId(cart.getCartId(), product.getProductId());

        assertNotNull(resultDTO);
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    void updateProductQuantity_shouldThrowException_whenNewQuantityNegative() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(item);

        assertThrows(APIException.class,
                () -> cartService.updateProductQuantity(1L, -2, user));
    }

    @Test
    void updateProductQuantity_shouldThrowException_whenNewQuantityGreaterThanStock() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(item);

        assertThrows(APIException.class,
                () -> cartService.updateProductQuantity(1L, 10, user));
    }


    @Test
    void updateProductQuantity_shouldThrowException_whenCartItemNotFound() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductQuantity(1L, 1, user));
    }

    @Test
    void updateProductQuantity_shouldThrowException_whenProductNotFound() {
        User user = getUser();
        Product product = getProduct();
        Cart cart = getCart(user);

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductQuantity(1L, 1, user));
    }

    @Test
    void updateProductQuantity_shouldThrowException_whenCartNotFound() {
        User user = getUser();

        when(cartRepository.findByUser(user)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductQuantity(1L, 1, user));
    }
    // -------------------- deleteProductFromCart --------------------

    @Test
    void deleteProductFromCart_shouldDeleteSuccessfully() {
        Cart cart = getCart(getUser());
        Product product = getProduct();

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductPrice(100.0);

        cart.setTotalPrice(200.0);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(item);

        String result = cartService.deleteProductFromCart(1L, 1L);

        verify(cartItemRepository).deleteCartItemByCartIdAndProductId(1L, 1L);

        assertTrue(result.contains("removed"));
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    void deleteProductFromCart_shouldThrowException_whenCartItemNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(new Cart()));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.deleteProductFromCart(1L, 1L));
    }

    @Test
    void deleteProductFromCart_shouldThrowException_whenCartNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.deleteProductFromCart(1L, 1L));
    }

    //----------------------updateProductInCart --------------------
    @Test
    void updateProductInCart_shouldUpdate(){
        Cart cart = getCart(getUser());
        Product product = getProduct();
        product.setSpecialPrice(50.0);
        product.setDiscount(20.0);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductPrice(100.0);

        cart.setTotalPrice(200.0);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(item);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.updateProductInCart(1L, 1L);

        assertEquals(100.0, cart.getTotalPrice());
        assertEquals(20.0, item.getDiscount());
    }

    @Test
    void updateProductInCart_shouldThrowException_whenCartNotFound(){
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductInCart(1L, 1L));
    }

    @Test
    void updateProductInCart_shouldThrowException_whenProductNotFound(){
        Cart cart = getCart(getUser());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductInCart(1L, 1L));
    }


    @Test
    void updateProductInCart_shouldThrowException_whenCartItemNotFound(){
        Cart cart = getCart(getUser());
        Product product = getProduct();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateProductInCart(1L, 1L));
    }

    // -------------------- getCart (internal) --------------------

    @Test
    void getCart_shouldCreateNewCart_whenNotExists() {
        User user = getUser();

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(null);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart cart = cartService.getCart();

        verify(cartRepository).save(any(Cart.class));
        assertEquals(0.0, cart.getTotalPrice());
        assertEquals(user, cart.getUser());
    }

    @Test
    void getCart_shouldCreateNewCart_whenCartExists() {
        User user = getUser();
        Cart cart = getCart(user);

        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(cart);

        Cart returnedCart = cartService.getCart();

        verify(cartRepository, Mockito.never()).save(any(Cart.class));
        assertNotNull(returnedCart);
    }
}