package com.electronicstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO để nhận dữ liệu từ form tạo/sửa category
 */
public class CategoryRequest {
    
    private Long id; // Null khi tạo mới, có giá trị khi cập nhật
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    private Boolean active = true; // Mặc định là active
    
    private MultipartFile imageFile; // File upload
    
    private String currentImageUrl; // URL hình ảnh hiện tại (cho trường hợp edit)
    
    // Constructors
    public CategoryRequest() {}
    
    public CategoryRequest(String name, String description, Boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public MultipartFile getImageFile() {
        return imageFile;
    }
    
    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
    
    public String getCurrentImageUrl() {
        return currentImageUrl;
    }
    
    public void setCurrentImageUrl(String currentImageUrl) {
        this.currentImageUrl = currentImageUrl;
    }
    
    @Override
    public String toString() {
        return "CategoryRequest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", currentImageUrl='" + currentImageUrl + '\'' +
                '}';
    }
}