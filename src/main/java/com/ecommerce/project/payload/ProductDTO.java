package com.ecommerce.project.payload;

import com.ecommerce.project.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private Long productId;
    @NotBlank
    @Size(min = 5, max = 30, message = "Product name should be between 5 and 30 characters")
    private String productName;
    private String image;

    @Size(min = 20, max = 5000, message = "Product description should be between 20 and 5000 characters")
    private String description;
    private Integer quantity;
    private Double price;
    private Double discount;
    private Double specialPrice;
}
