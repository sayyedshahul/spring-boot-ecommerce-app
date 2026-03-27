package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.ProductService;
import com.ecommerce.project.util.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProductService productService() {
            return Mockito.mock(ProductService.class);
        }

        @Bean
        public AuthUtil authUtil() {
            return Mockito.mock(AuthUtil.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public AuthTokenFilter authTokenFilter() {
            return Mockito.mock(AuthTokenFilter.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }

        @Bean
        public UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addProduct_shouldReturnCreatedProduct() throws Exception {
        ProductDTO request = new ProductDTO();
        request.setProductName("Laptop");

        ProductDTO response = new ProductDTO();
        response.setProductId(1L);
        response.setProductName("Laptop");

        User user = new User();
        when(authUtil.getLoggedInUser()).thenReturn(user);
        when(productService.addProduct(Mockito.anyLong(), Mockito.any(ProductDTO.class), Mockito.eq(user)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/categories/1/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    void getAllProducts_shouldReturnProductResponse() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setProductId(1L);

        ProductResponse response = new ProductResponse();
        response.setContent(List.of(product));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(productService.getAllProducts(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/public/products")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "productName")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));;
    }

    @Test
    void getProductsByCategory_shouldReturnProductResponse() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setProductId(1L);

        ProductResponse response = new ProductResponse();
        response.setContent(List.of(product));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(productService.getProductsByCategory(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/public/category/1/products")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "productId")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));;
    }

    @Test
    void getProductByKeyword_shouldReturnProductResponse() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setProductId(1L);

        ProductResponse response = new ProductResponse();
        response.setContent(List.of(product));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(productService.searchProductsByKeyword(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/public/products/keyword/laptop")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "productId")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));;
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        ProductDTO request = new ProductDTO();
        request.setProductName("Laptop Updated");

        ProductDTO response = new ProductDTO();
        response.setProductId(1L);
        response.setProductName("Laptop Updated");

        when(productService.updateProduct(Mockito.eq(1L), Mockito.any(ProductDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop Updated"));
    }

    @Test
    void deleteProduct_shouldReturnDeletedProduct() throws Exception {
        ProductDTO response = new ProductDTO();
        response.setProductId(1L);

        when(productService.deleteProduct(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void updateProductImage_shouldReturnUpdatedProduct() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg",
                "image/jpeg", "dummy-image-content".getBytes());

        ProductDTO response = new ProductDTO();
        response.setProductId(1L);

        when(productService.updateProductImage(Mockito.eq(1L), Mockito.any())).thenReturn(response);

        mockMvc.perform(multipart("/api/admin/products/1/image")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT"); // MockMvc multipart defaults to POST, so override to PUT
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.image", not(is("default.png"))));;
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductDTO response = new ProductDTO();
        response.setProductId(1L);

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/public/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }
}