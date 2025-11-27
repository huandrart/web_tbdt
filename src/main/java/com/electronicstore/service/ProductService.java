package com.electronicstore.service;

import com.electronicstore.entity.Product;
import com.electronicstore.entity.Category;
import com.electronicstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public List<Product> findActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }
    
    // CODE ĐÚNG:
    public Page<Product> findActiveProductsWithPagination(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable); // SỬA THÀNH CÁI NÀY
    }
    
    public Product findById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }
    
    public Optional<Product> findByIdOptional(Long id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }
    
    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategory(category);
    }
    
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    public Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable) {
        return productRepository.findByCategoryAndIsActiveTrue(category, pageable);
    }
    
    public Page<Product> findActiveProducts(Category category, Pageable pageable) {
        return productRepository.findByCategoryAndIsActiveTrue(category, pageable);
    }
    
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            keyword, keyword, pageable);
    }
    
    public Page<Product> searchProducts(String keyword, Long categoryId, Boolean isActive, Pageable pageable) {
        // If no filters applied, return all products
        if ((keyword == null || keyword.trim().isEmpty()) && 
            categoryId == null && isActive == null) {
            return productRepository.findAll(pageable);
        }
        
        // Apply filters based on what's provided
        if (keyword != null && !keyword.trim().isEmpty() && categoryId != null && isActive != null) {
            // All filters - use basic search for now
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable);
        } else if (keyword != null && !keyword.trim().isEmpty() && categoryId != null) {
            // Search + Category - use basic search for now
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Search only
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable);
        } else if (categoryId != null && isActive != null) {
            // Category + Status
            return productRepository.findByCategoryId(categoryId, pageable);
        } else if (categoryId != null) {
            // Category only
            return productRepository.findByCategoryId(categoryId, pageable);
        } else if (isActive != null) {
            // Status only
            if (isActive) {
                return productRepository.findByIsActiveTrue(pageable);
            } else {
                return productRepository.findByIsActiveFalse(pageable);
            }
        }
        
        return productRepository.findAll(pageable);
    }
    
    public List<Product> findFeaturedProducts() {
        return productRepository.findFeaturedProducts();
    }
    
    public Page<Product> findFeaturedProductsWithPagination(Pageable pageable) {
        return productRepository.findFeaturedProducts(pageable);
    }
    
    public List<Product> findPopularProducts(Pageable pageable) {
        return productRepository.findPopularProducts(pageable);
    }
    
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    public Page<Product> findByCategoryAndPriceRange(Category category, BigDecimal minPrice, 
                                                     BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable);
    }
    
    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrandAndIsActiveTrue(brand);
    }
    
    public List<String> findAllBrands() {
        return productRepository.findAllBrands();
    }

    public Page<Product> findByBrandWithPagination(String brand, Pageable pageable) {
        return productRepository.findByBrandAndIsActiveTrue(brand, pageable);
    }
    
    public List<Product> findInStockProducts() {
        return productRepository.findInStockProducts();
    }
    
    public List<Product> findLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    public Product update(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID không được null khi cập nhật");
        }
        return productRepository.save(product);
    }
    
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
    
    public void softDeleteById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product prod = product.get();
            prod.setIsActive(false);
            productRepository.save(prod);
        }
    }
    
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
    
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
    
    public void incrementViewCount(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.incrementViewCount();
            productRepository.save(product);
        }
    }
    
    public void updateStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newStock = product.getStockQuantity() - quantity;
            if (newStock < 0) {
                throw new IllegalArgumentException("Không đủ hàng trong kho");
            }
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }
    
}