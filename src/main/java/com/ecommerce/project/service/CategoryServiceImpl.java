package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{
    List<Category> categories = new ArrayList<>();
    private long nextId = 1L;

    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public void createCategory(Category category) {
        category.setCategoryId(nextId++);
        categories.add(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        if(categories.removeIf(x -> x.getCategoryId() == categoryId))
            return "Category with categoryId " + categoryId + " deleted successfully.";
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Override
    public String updateCategory(Category category, long categoryId) {
        Optional<Category> optionalCategory = categories.stream()
                                                .filter(x -> x.getCategoryId() == categoryId)
                                                .findFirst();
        if(optionalCategory.isPresent()){
            Category foundCategory = optionalCategory.get();
            foundCategory.setCategoryName(category.getCategoryName());
            return "Category with categoryId " + categoryId + " updated successfully.";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }
}
