package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        List<Category> categories = categoryRepository.findAll();

        Optional<Category> optionalCategory = categories.stream()
                .filter(x -> x.getCategoryId() == categoryId)
                .findFirst();

        if(optionalCategory.isPresent()) {
            categoryRepository.delete(optionalCategory.get());
            return "Category with categoryId " + categoryId + " deleted successfully.";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Override
    public String updateCategory(Category category, long categoryId) {
        List<Category> categories = categoryRepository.findAll();

        Optional<Category> optionalCategory = categories.stream()
                .filter(x -> x.getCategoryId() == categoryId)
                .findFirst();

        if(optionalCategory.isPresent()){
            Category foundCategory = optionalCategory.get();
            foundCategory.setCategoryName(category.getCategoryName());
            categoryRepository.save(foundCategory);
            return "Category with categoryId " + categoryId + " updated successfully.";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }
}
