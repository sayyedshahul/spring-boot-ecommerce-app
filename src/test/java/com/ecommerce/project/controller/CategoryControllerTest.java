package com.ecommerce.project.controller;

import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.CategoryService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CategoryControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }

        @Bean
        public AuthTokenFilter authTokenFilter() {
            return Mockito.mock(AuthTokenFilter.class);
        }

        @Bean
        public UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_shouldReturnCategoryResponse() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setContent(List.of(new CategoryDTO(1L, "Electronics")));
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setTotalElements(1);
        response.setTotalPages(1);

        when(categoryService.getAllCategories(
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenReturn(response);

        mockMvc.perform(get("/api/public/categories")
                        .param("pageSize", "10")
                        .param("pageNumber", "0")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryId").value(1))
                .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Books");

        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(2L);
        response.setCategoryName("Books");

        when(categoryService.createCategory(Mockito.any(CategoryDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.categoryName").value("Books"));
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Home Appliances");

        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(1L);
        response.setCategoryName("Home Appliances");

        when(categoryService.updateCategory(Mockito.any(CategoryDTO.class), Mockito.eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Home Appliances"));
    }

    @Test
    void deleteCategory_shouldReturnDeletedCategory() throws Exception {
        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(1L);
        response.setCategoryName("Toys");

        when(categoryService.deleteCategory(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Toys"));
    }
}