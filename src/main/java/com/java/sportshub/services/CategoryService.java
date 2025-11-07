package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CategoryDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Category;
import com.java.sportshub.models.Product;

@Service
public class CategoryService {

    @Autowired
    private CategoryDAO categoryDAO;

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Transactional
    public Category createCategory(Category category) {
        validateCategory(category);

        if (categoryDAO.existsByName(category.getName())) {
            throw new AttributeExistsException("Category", "name", category.getName());
        }

        return categoryDAO.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);

        if (categoryDetails.getName() != null) {
            // Verificar si el nuevo nombre ya existe en otra categoría
            if (categoryDAO.existsByName(categoryDetails.getName()) &&
                    !category.getName().equals(categoryDetails.getName())) {
                throw new AttributeExistsException("Category", "name", categoryDetails.getName());
            }
            category.setName(categoryDetails.getName());
        }

        return categoryDAO.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(false);
        categoryDAO.save(category);
    }

    public List<Product> getProductsByCategoryId(Long id) {
        Category category = getCategoryById(id);
        return category.getProducts();
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
    }
}
