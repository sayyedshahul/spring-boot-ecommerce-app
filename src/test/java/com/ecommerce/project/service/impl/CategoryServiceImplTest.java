package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryServiceImpl(categoryRepository, modelMapper);
    }

    @Test
    void shouldReturnAllCategories_WhenCategoriesExist(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "categoryName";
        String sortOrder = "asc";

        Category category1 = new Category();
        category1.setCategoryId(1L);
        category1.setCategoryName("Homes");

        List<Category> categories = List.of(category1);

        Page<Category> categoryPage = new PageImpl<>(categories, PageRequest.of(pageNumber, pageSize), 1);

        Mockito.when(categoryRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(categoryPage);

        CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(categoryRepository).findAll(captor.capture());
        Pageable capturedPageable = captor.getValue();

        Assertions.assertEquals(pageNumber, capturedPageable.getPageNumber());
        Assertions.assertEquals(pageSize, capturedPageable.getPageSize());
        Sort.Order order = capturedPageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(Sort.Direction.ASC, order.getDirection());


        Assertions.assertEquals(1, response.getContent().size());
        Assertions.assertEquals(pageNumber, response.getPageNumber());
        Assertions.assertEquals(pageSize, response.getPageSize());
        Assertions.assertEquals(1, response.getTotalElements());
        Assertions.assertEquals(1 ,response.getTotalPages());
        Assertions.assertTrue(response.getLastPages());
    }

    @Test
    void shouldReturnEmptyCategoryList_WhenNoCategoriesExist(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "categoryName";
        String sortOrder = "asc";

        Page<Category> categoryPage = new PageImpl<>(List.of(), PageRequest.of(pageNumber, pageSize), 0);

        Mockito.when(categoryRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(categoryPage);

        CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

        Mockito.verify(categoryRepository).findAll(Mockito.any(Pageable.class));

        Assertions.assertEquals(0, response.getContent().size());
        Assertions.assertEquals(0, response.getTotalElements());
    }

    @Test
    void shouldCreateCategory_WhenCategoryDoesntExistAlready(){
        CategoryDTO inputCategoryDTO = new CategoryDTO();
        inputCategoryDTO.setCategoryName("Homes");

        Category existingCategory = new Category();
        existingCategory.setCategoryName("Homes");
        existingCategory.setCategoryId(1L);

        Mockito.when(categoryRepository.findByCategoryName(Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(existingCategory);

        CategoryDTO savedCategory = categoryService.createCategory(inputCategoryDTO);

        Mockito.verify(categoryRepository).save(Mockito.any(Category.class));

        Assertions.assertNotNull(savedCategory.getCategoryId());
        Assertions.assertEquals(inputCategoryDTO.getCategoryName(), savedCategory.getCategoryName());
    }

    @Test
    void shouldThrowExceptionInCreatingCategory_WhenCategoryExistAlready(){
        CategoryDTO inputCategoryDTO = new CategoryDTO();
        inputCategoryDTO.setCategoryName("Homes");

        Category existingCategory = new Category();
        existingCategory.setCategoryName("Homes");
        existingCategory.setCategoryId(1L);

        Mockito.when(categoryRepository.findByCategoryName(Mockito.anyString()))
                .thenReturn(existingCategory);

        Assertions.assertThrows(APIException.class, () ->
                categoryService.createCategory(inputCategoryDTO));
        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any(Category.class));
    }

    @Test
    void shouldDeleteCategory_WhenCategoryExists(){
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setCategoryName("Homes");
        existingCategory.setCategoryId(1L);

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(existingCategory));

        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryRepository).delete(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        Assertions.assertEquals(categoryId, capturedCategory.getCategoryId());
        Assertions.assertEquals(categoryId, categoryDTO.getCategoryId());
    }

    @Test
    void shouldThrowExceptionInDeleteCategory_WhenCategoryDoesntExist(){
        Long categoryId = 1L;

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                categoryService.deleteCategory(categoryId));
        Mockito.verify(categoryRepository, Mockito.never()).delete(Mockito.any(Category.class));
    }

    @Test
    void shouldUpdateCategory_WhenCategoryExists(){
        Long categoryId = 1L;
        CategoryDTO inputCategoryDTO = new CategoryDTO();
        inputCategoryDTO.setCategoryName("Homes - Updated");

        Category existingCategory = new Category();
        existingCategory.setCategoryName("Homes");
        existingCategory.setCategoryId(1L);

        Category savedCategory = new Category();
        savedCategory.setCategoryName("Homes - Updated");
        savedCategory.setCategoryId(1L);

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(existingCategory));
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(savedCategory);

        CategoryDTO categoryDTO = categoryService.updateCategory(inputCategoryDTO, categoryId);
        Assertions.assertEquals(inputCategoryDTO.getCategoryName(), categoryDTO.getCategoryName());

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        Assertions.assertEquals(inputCategoryDTO.getCategoryName(), capturedCategory.getCategoryName());
    }

    @Test
    void shouldThrowExceptionInUpdateCategory_WhenCategoryDoesntExist(){
        Long categoryId = 1L;
        CategoryDTO inputCategoryDTO = new CategoryDTO();
        inputCategoryDTO.setCategoryName("Homes - Updated");

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                categoryService.updateCategory(inputCategoryDTO, categoryId));
        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any(Category.class));
    }
}
