package com.ecommerce.project.payload;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SellerOrderResponseDTO {
    private List<OrderItemDTO> sellerOrderItems; // Order Items for a particular seller.
}
