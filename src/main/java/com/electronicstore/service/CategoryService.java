package com.electronicstore.service;

import com.electronicstore.entity.Category;
import com.electronicstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    public Category findById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElse(null);
    }
    
    public Optional<Category> findByIdOptional(Long id) {
        return categoryRepository.findById(id);
    }
    
    public List<Category> findActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }
    
    public List<Category> findActiveCategoriesOrderByName() {
        return categoryRepository.findActiveCategoriesOrderByName();
    }
    
    public Optional<Category> findByIdWithProducts(Long id) {
        return categoryRepository.findByIdWithProducts(id);
    }
    
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public long countProductsByCategory(Category category) {
        return categoryRepository.countProductsByCategory(category);
    }
    
    public List<Category> findActiveCategoriesWithProducts() {
        return categoryRepository.findActiveCategoriesWithProducts();
    }
    
    public Category findByNameDirect(String name) {
        Optional<Category> category = categoryRepository.findByName(name);
        return category.orElse(null);
    }
    
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category update(Category category) {
        if (category.getId() == null) {
            throw new IllegalArgumentException("Category ID không được null khi cập nhật");
        }
        return categoryRepository.save(category);
    }
    
    public void deleteById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category cat = category.get();
            cat.setIsActive(false);
            categoryRepository.save(cat);
        }
    }
    
    public void hardDeleteById(Long id) {
        categoryRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    public boolean existsByNameAndIdNot(String name, Long id) {
        Optional<Category> existing = categoryRepository.findByName(name);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }
    
    public long count() {
        return categoryRepository.count();
    }
    
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }
    
    public Page<Category> searchCategories(String search, Boolean isActive, Pageable pageable) {
        if (search != null && !search.trim().isEmpty() && isActive != null) {
            // Search by name and filter by status
            return categoryRepository.findByNameContainingIgnoreCaseAndIsActive(search.trim(), isActive, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search by name only
            return categoryRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (isActive != null) {
            // Filter by status only
            return categoryRepository.findByIsActive(isActive, pageable);
        } else {
            // No filters
            return categoryRepository.findAll(pageable);
        }
    }
}