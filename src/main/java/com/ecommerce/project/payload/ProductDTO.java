package com.ecommerce.project.payload;

import com.ecommerce.project.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private Long productId;
    private String productName;
    private String image;
    private String description;
    private Integer quantity;
    private Double price;
    private Double discount;
    private Double specialPrice;
}
