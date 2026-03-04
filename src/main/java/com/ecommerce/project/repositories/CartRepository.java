package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c from Cart c where c.user.email = ?1")
    Cart findCartByUserEmail(String email);

    @Query("SELECT c from Cart c where c.user.email = ?1 and c.cartId = ?2")
    Optional<Cart> findCartByUserEmailAndCartId(String email, Long cartId);

    Cart findByUser(User user);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p where p.productId = ?1")
    List<Cart> findCartsByProduct(Long productId);
}
