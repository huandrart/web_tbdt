package com.electronicstore.viewmodel;

import com.electronicstore.dto.request.ProductRequest;
import com.electronicstore.dto.response.CategoryResponse;

import java.util.List;

/**
 * ViewModel cho trang tạo/sửa product
 */
public class ProductFormViewModel {
    
    private ProductRequest productRequest;
    private List<CategoryResponse> categories; // Danh sách categories để chọn
    private boolean isEditMode;
    private String pageTitle;
    private String submitButtonText;
    private String cancelUrl;
    
    // Constructors
    public ProductFormViewModel() {}
    
    public ProductFormViewModel(boolean isEditMode) {
        this.isEditMode = isEditMode;
        this.productRequest = new ProductRequest();
        this.pageTitle = isEditMode ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới";
        this.submitButtonText = isEditMode ? "Cập nhật" : "Thêm mới";
        this.cancelUrl = "/admin/products";
    }
    
    public ProductFormViewModel(ProductRequest productRequest, boolean isEditMode) {
        this.productRequest = productRequest;
        this.isEditMode = isEditMode;
        this.pageTitle = isEditMode ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới";
        this.submitButtonText = isEditMode ? "Cập nhật" : "Thêm mới";
        this.cancelUrl = "/admin/products";
    }
    
    // Getters and Setters
    public ProductRequest getProductRequest() {
        return productRequest;
    }
    
    public void setProductRequest(ProductRequest productRequest) {
        this.productRequest = productRequest;
    }
    
    // Alias method for compatibility
    public ProductRequest getProduct() {
        return productRequest;
    }
    
    public void setProduct(ProductRequest productRequest) {
        this.productRequest = productRequest;
    }
    
    public List<CategoryResponse> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
    
    public boolean isEditMode() {
        return isEditMode;
    }
    
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }
    
    // Alias methods for compatibility
    public String getMode() {
        return isEditMode ? "edit" : "create";
    }
    
    public void setMode(String mode) {
        this.isEditMode = "edit".equals(mode);
    }
    
    public String getPageTitle() {
        return pageTitle;
    }
    
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
    
    // Alias method for compatibility
    public String getTitle() {
        return pageTitle;
    }
    
    public void setTitle(String title) {
        this.pageTitle = title;
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
    
    // Additional methods for form compatibility
    public String getAction() {
        return isEditMode ? "/admin/products/" + (productRequest != null ? productRequest.getId() : "") : "/admin/products";
    }
    
    public void setAction(String action) {
        // This can be used if needed for custom actions
    }
    
    public String getMethod() {
        return isEditMode ? "PUT" : "POST";
    }
    
    public void setMethod(String method) {
        // This can be used if needed for custom methods
    }
}