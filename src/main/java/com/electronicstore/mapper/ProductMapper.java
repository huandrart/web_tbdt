package com.electronicstore.mapper;

import com.electronicstore.dto.response.ProductResponse;
import com.electronicstore.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Mapper class for converting between Product entity and DTOs
 */
@Component
public class ProductMapper {
    
    /**
     * Convert Product entity to ProductResponse DTO
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductResponse response = new ProductResponse();
        
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setModel(product.getModel());
        response.setPrice(product.getPrice());
        response.setSalePrice(product.getSalePrice());
        response.setStock(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setFeatured(product.getIsFeatured());
        response.setActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // Set category information
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        
        // Calculate discount percentage
        if (product.getSalePrice() != null && product.getPrice() != null && 
            product.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = product.getPrice().subtract(product.getSalePrice());
            BigDecimal discountPercent = discount.divide(product.getPrice(), 4, RoundingMode.HALF_UP)
                                              .multiply(new BigDecimal("100"));
            response.setDiscountPercent(discountPercent);
        } else {
            response.setDiscountPercent(BigDecimal.ZERO);
        }
        
        // Set stock status
        response.setInStock(product.getStockQuantity() != null && product.getStockQuantity() > 0);
        if (product.getStockQuantity() != null) {
            if (product.getStockQuantity() == 0) {
                response.setStockStatus("Hết hàng");
            } else if (product.getStockQuantity() <= 10) {
                response.setStockStatus("Sắp hết");
            } else {
                response.setStockStatus("Còn hàng");
            }
        } else {
            response.setStockStatus("Không xác định");
        }
        
        return response;
    }
    
    // ProductRequest methods removed - using simple approach
    
    // ProductRequest methods removed - using simple approach
}