package com.electronicstore.service;

import com.electronicstore.dto.request.CategoryRequest;
import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.viewmodel.CategoryFormViewModel;
import com.electronicstore.viewmodel.CategoryListViewModel;

/**
 * Business Service interface cho Category management
 */
public interface CategoryBusinessService {
    
    /**
     * Lấy danh sách categories với phân trang và tìm kiếm
     */
    CategoryListViewModel getCategoryList(int page, int size, String sort, String direction, 
                                        String search, String status);
    
    /**
     * Tạo ViewModel cho form thêm mới category
     */
    CategoryFormViewModel createNewCategoryForm();
    
    /**
     * Tạo ViewModel cho form chỉnh sửa category
     */
    CategoryFormViewModel createEditCategoryForm(Long categoryId);
    
    /**
     * Lưu category (tạo mới hoặc cập nhật)
     */
    CategoryResponse saveCategory(CategoryRequest request);
    
    /**
     * Xóa category
     */
    void deleteCategory(Long categoryId);
    
    /**
     * Lấy thông tin chi tiết category
     */
    CategoryResponse getCategoryDetail(Long categoryId);
    
    /**
     * Thay đổi trạng thái active/inactive của category
     */
    boolean toggleCategoryStatus(Long categoryId);
    
    /**
     * Kiểm tra xem category có thể xóa được không
     */
    boolean canDeleteCategory(Long categoryId);
}