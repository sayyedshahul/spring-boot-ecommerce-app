package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, ProductDTO productDTO, User user);

    ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder);

    ProductResponse getProductsByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder);

    ProductResponse searchProductsByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder);

    ProductDTO updateProduct(Long productId, ProductDTO productDTO);

    ProductDTO deleteProduct(Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
}
