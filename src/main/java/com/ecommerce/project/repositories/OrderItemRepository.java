package com.ecommerce.project.repositories;

import com.ecommerce.project.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi from OrderItem oi where oi.product.user.email = ?1")
    Page<OrderItem> findOrdersBySellerEmail(String email, Pageable pageDetails);
}