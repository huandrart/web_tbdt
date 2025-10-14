package com.electronicstore.repository;

import com.electronicstore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrue();
    
    Optional<Category> findByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name")
    List<Category> findActiveCategoriesOrderByName();
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(Long id);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category AND p.isActive = true")
    long countProductsByCategory(Category category);
    
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE c.isActive = true AND p.isActive = true ORDER BY c.name")
    List<Category> findActiveCategoriesWithProducts();
    
    boolean existsByName(String name);
    
    // Search and filter methods
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Category> findByNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive, Pageable pageable);
}