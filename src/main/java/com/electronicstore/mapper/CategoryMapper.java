package com.electronicstore.mapper;

import com.electronicstore.dto.request.CategoryRequest;
import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.entity.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper để chuyển đổi giữa Entity và DTO
 */
@Component
public class CategoryMapper {
    
    /**
     * Chuyển đổi từ Entity sang Response DTO
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Đếm số sản phẩm trong danh mục
        if (category.getProducts() != null) {
            response.setProductCount(category.getProducts().size());
        } else {
            response.setProductCount(0);
        }
        
        return response;
    }
    
    /**
     * Chuyển đổi từ Entity sang Request DTO (để edit form)
     */
    public CategoryRequest toRequest(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryRequest request = new CategoryRequest();
        request.setId(category.getId());
        request.setName(category.getName());
        request.setDescription(category.getDescription());
        request.setActive(category.getIsActive());
        request.setCurrentImageUrl(category.getImageUrl());
        
        return request;
    }
    
    /**
     * Chuyển đổi từ Request DTO sang Entity (để save)
     */
    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        Category category = new Category();
        category.setId(request.getId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getActive());
        
        return category;
    }
    
    /**
     * Cập nhật Entity từ Request DTO (để update)
     */
    public void updateEntity(Category category, CategoryRequest request) {
        if (category == null || request == null) {
            return;
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getActive());
        
        // Không cập nhật ID, createdAt, updatedAt ở đây
        // updatedAt sẽ được cập nhật tự động trong entity lifecycle
    }
}