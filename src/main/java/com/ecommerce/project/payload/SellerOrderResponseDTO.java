package com.ecommerce.project.payload;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SellerOrderResponseDTO {
    private List<OrderItemDTO> content;// Order Items for a particular seller.
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
