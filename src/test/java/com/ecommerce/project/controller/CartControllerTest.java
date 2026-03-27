package com.ecommerce.project.controller;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.UpdateCartQuantityDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.CartService;
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

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CartControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CartService cartService() {
            return Mockito.mock(CartService.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }

        @Bean
        public AuthUtil authUtils() {
            return Mockito.mock(AuthUtil.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public CartRepository cartRepository() {
            return Mockito.mock(CartRepository.class);
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUtil authUtils;

    @Autowired
    private CartRepository cartRepository;

    @Test
    void addProductToCart_shouldReturnCartDTO() throws Exception{
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(1L);

        when(cartService.addProductToCart(1L, 5)).thenReturn(cartDTO);

        mockMvc.perform(post("/api/carts/products/1/quantity/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value("1"));
    }

    @Test
    void getAllCarts_shouldReturnList() throws Exception {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(1L);

        when(cartService.getAllCarts()).thenReturn(List.of(cartDTO));

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartId").value(1));
    }

    @Test
    void getCartById_shouldReturnCartDTO() throws Exception {

        Cart cart = new Cart();
        cart.setCartId(1L);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(1L);

        when(authUtils.getLoggedInEmail()).thenReturn("test@mail.com");
        when(cartRepository.findCartByUserEmail("test@mail.com")).thenReturn(cart);
        when(cartService.getCart("test@mail.com", 1L)).thenReturn(cartDTO);

        mockMvc.perform(get("/api/carts/users/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));
    }

    @Test
    void updateProductQuantity_shouldReturnUpdatedCart() throws Exception {

        UpdateCartQuantityDTO request = new UpdateCartQuantityDTO();
        request.setQuantity(3);

        User user = new User();

        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(1L);

        when(authUtils.getLoggedInUser()).thenReturn(user);
        when(cartService.updateProductQuantity(Mockito.anyLong(), Mockito.anyInt(), Mockito.any(User.class)))
                .thenReturn(cartDTO);

        mockMvc.perform(put("/api/carts/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));
    }

    @Test
    void deleteProductFromCart_shouldReturnSuccessMessage() throws Exception {

        String message = "Product Laptop removed from the cart !!!";

        when(cartService.deleteProductFromCart(1L, 1L)).thenReturn(message);

        mockMvc.perform(delete("/api/carts/1/products/1"))
                .andExpect(status().isOk());
    }
}
