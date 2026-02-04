package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
        Category foundCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(!(foundCategory == null)){
            throw new APIException("Category with category name:" + category.getCategoryName() + " already exists.");
        }
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category foundCategory =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(foundCategory);
        return "Category with categoryId " + categoryId + " deleted successfully.";
    }

    @Override
    public String updateCategory(Category category, long categoryId) {
        Category foundCategory =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        foundCategory.setCategoryName(category.getCategoryName());
        categoryRepository.save(foundCategory);
        return "Category with categoryId " + categoryId + " updated successfully.";
    }
}
