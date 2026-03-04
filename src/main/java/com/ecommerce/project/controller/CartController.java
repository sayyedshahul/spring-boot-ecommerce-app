package com.ecommerce.project.controller;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final AuthUtil authUtils;
    private final CartRepository cartRepository;

    @PostMapping("/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity){
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts(){
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }

    @GetMapping("/users/cart")
    public ResponseEntity<CartDTO> getCartById(){
        String emailId = authUtils.getLoggedInEmail();
        Cart cart = cartRepository.findCartByUserEmail(emailId);
        CartDTO cartDTO = cartService.getCart(emailId, cart.getCartId());
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/products/{productId}/operation/{operation}")
    public ResponseEntity<CartDTO> updateProductQuantityInCart(@PathVariable Long productId, @PathVariable String operation){
        Integer quantity = operation.equalsIgnoreCase("delete") ? -1 : 1;
        CartDTO cartDTO = cartService.updateProductQuantity(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId){
        String message = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
