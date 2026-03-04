package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private CartDTO cartDTO;
    private ProductDTO productDTO;
    private Double productPrice;
    private Double discount;
    private Integer quantity;
}
