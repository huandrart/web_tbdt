package com.electronicstore.service.impl;

import com.electronicstore.dto.request.CategoryRequest;
import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.entity.Category;
import com.electronicstore.mapper.CategoryMapper;
import com.electronicstore.service.CategoryBusinessService;
import com.electronicstore.service.CategoryService;
import com.electronicstore.service.FileStorageService;
import com.electronicstore.viewmodel.CategoryFormViewModel;
import com.electronicstore.viewmodel.CategoryListViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business Service implementation cho Category management
 */
@Service
@Transactional
public class CategoryBusinessServiceImpl implements CategoryBusinessService {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public CategoryListViewModel getCategoryList(int page, int size, String sort, String direction, 
                                               String search, String status) {
        
        // Tạo Pageable
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Lấy dữ liệu từ database
        Page<Category> categoryPage;
        
        if ((search != null && !search.trim().isEmpty()) || 
            (status != null && !status.isEmpty())) {
            
            Boolean isActive = null;
            if (status != null && !status.isEmpty()) {
                isActive = Boolean.parseBoolean(status);
            }
            
            categoryPage = categoryService.searchCategories(search, isActive, pageable);
        } else {
            categoryPage = categoryService.findAll(pageable);
        }
        
        // Chuyển đổi Entity sang DTO
        Page<CategoryResponse> categoryResponsePage = categoryPage.map(categoryMapper::toResponse);
        
        // Tạo ViewModel
        CategoryListViewModel viewModel = new CategoryListViewModel(categoryResponsePage);
        viewModel.setCurrentSort(sort);
        viewModel.setCurrentDirection(direction);
        viewModel.setCurrentSearch(search);
        viewModel.setCurrentStatus(status);
        
        // Tạo danh sách số trang cho pagination
        List<Integer> pageNumbers = generatePageNumbers(page, categoryResponsePage.getTotalPages());
        viewModel.setPageNumbers(pageNumbers);
        
        return viewModel;
    }
    
    @Override
    public CategoryFormViewModel createNewCategoryForm() {
        CategoryFormViewModel viewModel = new CategoryFormViewModel(false);
        viewModel.setCategoryRequest(new CategoryRequest());
        
        // Lấy danh sách parent categories nếu cần
        List<Category> activeCategories = categoryService.findActiveCategories();
        List<CategoryResponse> parentCategories = activeCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setParentCategories(parentCategories);
        
        return viewModel;
    }
    
    @Override
    public CategoryFormViewModel createEditCategoryForm(Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId);
        }
        
        CategoryRequest categoryRequest = categoryMapper.toRequest(category);
        CategoryFormViewModel viewModel = new CategoryFormViewModel(categoryRequest, true);
        
        // Lấy danh sách parent categories nếu cần
        List<Category> activeCategories = categoryService.findActiveCategories();
        List<CategoryResponse> parentCategories = activeCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setParentCategories(parentCategories);
        
        return viewModel;
    }
    
    @Override
    public CategoryResponse saveCategory(CategoryRequest request) {
        try {
            Category category;
            
            if (request.getId() != null) {
                // Cập nhật category existing
                category = categoryService.findById(request.getId());
                if (category == null) {
                    throw new RuntimeException("Không tìm thấy danh mục với ID: " + request.getId());
                }
                categoryMapper.updateEntity(category, request);
            } else {
                // Tạo mới category
                category = categoryMapper.toEntity(request);
            }
            
            // Xử lý upload image
            if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                String imageUrl = fileStorageService.saveCategoryImage(request.getImageFile());
                category.setImageUrl(imageUrl);
            }
            
            // Lưu vào database
            Category savedCategory = categoryService.save(category);
            
            return categoryMapper.toResponse(savedCategory);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu danh mục: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId);
        }
        
        // Kiểm tra xem có thể xóa không
        if (!canDeleteCategory(categoryId)) {
            throw new RuntimeException("Không thể xóa danh mục này vì còn có sản phẩm liên quan");
        }
        
        // Xóa file image nếu có
        if (category.getImageUrl() != null) {
            fileStorageService.deleteFile(category.getImageUrl());
        }
        
        // Hard delete - xóa hẳn khỏi database
        categoryService.hardDeleteById(categoryId);
    }
    
    @Override
    public CategoryResponse getCategoryDetail(Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId);
        }
        
        return categoryMapper.toResponse(category);
    }
    
    @Override
    public boolean toggleCategoryStatus(Long categoryId) {
        try {
            Category category = categoryService.findById(categoryId);
            if (category == null) {
                return false;
            }
            
            category.setIsActive(!category.getIsActive());
            categoryService.save(category);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean canDeleteCategory(Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return false;
        }
        
        // Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        return category.getProducts() == null || category.getProducts().isEmpty();
    }
    
    @Override
    public Category getCategoryWithDetails(Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId);
        }
        return category;
    }
    
    /**
     * Tạo danh sách số trang cho pagination
     */
    private List<Integer> generatePageNumbers(int currentPage, int totalPages) {
        List<Integer> pageNumbers = new ArrayList<>();
        
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(i);
        }
        
        return pageNumbers;
    }
}