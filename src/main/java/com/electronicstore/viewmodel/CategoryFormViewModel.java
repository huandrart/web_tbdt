package com.electronicstore.viewmodel;

import com.electronicstore.dto.request.CategoryRequest;
import com.electronicstore.dto.response.CategoryResponse;

import java.util.List;

/**
 * ViewModel cho trang tạo/sửa category
 */
public class CategoryFormViewModel {
    
    private CategoryRequest categoryRequest;
    private List<CategoryResponse> parentCategories; // Danh sách danh mục cha (nếu có)
    private boolean isEditMode;
    private String pageTitle;
    private String submitButtonText;
    private String cancelUrl;
    
    // Constructors
    public CategoryFormViewModel() {}
    
    public CategoryFormViewModel(boolean isEditMode) {
        this.isEditMode = isEditMode;
        this.categoryRequest = new CategoryRequest();
        this.pageTitle = isEditMode ? "Chỉnh sửa danh mục" : "Thêm danh mục mới";
        this.submitButtonText = isEditMode ? "Cập nhật" : "Thêm mới";
        this.cancelUrl = "/admin/categories";
    }
    
    public CategoryFormViewModel(CategoryRequest categoryRequest, boolean isEditMode) {
        this.categoryRequest = categoryRequest;
        this.isEditMode = isEditMode;
        this.pageTitle = isEditMode ? "Chỉnh sửa danh mục" : "Thêm danh mục mới";
        this.submitButtonText = isEditMode ? "Cập nhật" : "Thêm mới";
        this.cancelUrl = "/admin/categories";
    }
    
    // Getters and Setters
    public CategoryRequest getCategoryRequest() {
        return categoryRequest;
    }
    
    public void setCategoryRequest(CategoryRequest categoryRequest) {
        this.categoryRequest = categoryRequest;
    }
    
    public List<CategoryResponse> getParentCategories() {
        return parentCategories;
    }
    
    public void setParentCategories(List<CategoryResponse> parentCategories) {
        this.parentCategories = parentCategories;
    }
    
    public boolean isEditMode() {
        return isEditMode;
    }
    
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }
    
    public String getPageTitle() {
        return pageTitle;
    }
    
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
    
    public String getSubmitButtonText() {
        return submitButtonText;
    }
    
    public void setSubmitButtonText(String submitButtonText) {
        this.submitButtonText = submitButtonText;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
}