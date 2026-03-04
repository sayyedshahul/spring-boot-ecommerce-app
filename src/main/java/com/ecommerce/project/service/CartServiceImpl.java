package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
//import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{

    private final AuthUtil authUtils;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find the cart, if it does not exist create one.
        Cart cart = getCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);

        if(cartItem != null){
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }
        if(product.getQuantity() == 0){
            throw new APIException("Product " + product.getProductName() + " is out of stock");
        }
        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        cart.getCartItems().add(newCartItem); // To maintain inverse relationship.

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        List<ProductDTO> productDTOs = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                }).toList();
        cartDTO.setProductDTOs(productDTOs);

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        List<CartDTO> cartDTOs = carts.stream().map( cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> productDTOs = cart.getCartItems()
                    .stream().map(item -> {
                          ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                          productDTO.setQuantity(item.getQuantity());
                          return productDTO;
                    }).toList();

            cartDTO.setProductDTOs(productDTOs);

            return cartDTO;
        }).toList();

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByUserEmailAndCartId(emailId, cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                }).toList();

        cartDTO.setProductDTOs(productDTOs);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantity(Long productId, Integer quantityChange) {
        User user = authUtils.getLoggedInUser();
        Cart cart = cartRepository.findByUser(user);

        if(cart == null){
            throw new ResourceNotFoundException("Cart", "user", user.getUserId());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if(cartItem == null){
            throw new ResourceNotFoundException("Product in cart", "productId", productId);
        }

        Integer newQuantity = cartItem.getQuantity() + quantityChange;

        if(newQuantity < 0){
            throw new APIException("Order quantity cannot be negative");
        }
        else if(newQuantity == 0){
            System.out.println("inside equals 0");
            cart.setTotalPrice(cart.getTotalPrice() -
                    (cartItem.getProductPrice() * cartItem.getQuantity()));
            cartItemRepository.deleteCartItemByCartIdAndProductId(cart.getCartId(), product.getProductId());
            return mapCartToDTO(cart);
        }
        else if(product.getQuantity() < newQuantity) { // check stock
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem.setQuantity(newQuantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * cartItem.getQuantity()));

        return mapCartToDTO(cart);
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByCartIdAndProductId(cartId, productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Transactional
    @Override
    public void updateProductInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "product", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if(cartItem == null)
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");


        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity())); // Negate old price.

        System.out.println(product.getSpecialPrice());
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setProductPrice(product.getDiscount());

        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * cartItem.getQuantity())); // Set new price
    }


    public CartDTO mapCartToDTO(Cart cart){
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(item -> { ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                }).toList();
        cartDTO.setProductDTOs(productDTOs);

        return cartDTO;
    }

    public Cart getCart(){
        Cart cart = cartRepository.findByUser(authUtils.getLoggedInUser());
        if(cart != null){
            return cart;
        }

        Cart newCart = new Cart();
        newCart.setTotalPrice(0.0);
        newCart.setUser(authUtils.getLoggedInUser());

        return cartRepository.save(newCart);
    }
}
