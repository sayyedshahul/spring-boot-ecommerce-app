package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                              @PathVariable Long categoryId){
        ProductDTO savedProductDTO = productService.addProduct(categoryId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder,
            @RequestParam(defaultValue = "productId") String sortBy
    ){
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/category/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder,
                                                                 @RequestParam(defaultValue = "productId") String sortBy){
        ProductResponse productResponse = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductByKeyword(@PathVariable String keyword,
                                                               @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
                                                               @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
                                                               @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder,
                                                               @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy){
        ProductResponse productResponse = productService.searchProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductDTO productDTO){
        ProductDTO savedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        ProductDTO productDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestParam MultipartFile image) throws IOException {
        ProductDTO updatedProductDTO = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }
}
