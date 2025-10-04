package com.electronicstore.util;

import org.springframework.stereotype.Component;

/**
 * Utility class để xử lý file và image
 */
@Component
public class FileUtil {
    
    // Default images
    public static final String DEFAULT_CATEGORY_IMAGE = "/static/images/default-category.svg";
    public static final String DEFAULT_PRODUCT_IMAGE = "/static/images/default-product.svg";
    public static final String DEFAULT_USER_AVATAR = "/static/images/default-avatar.svg";
    
    /**
     * Kiểm tra xem file có phải là image không
     */
    public static boolean isImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("webp") || extension.equals("bmp");
    }
    
    /**
     * Lấy extension của file
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * Tạo filename an toàn (loại bỏ các ký tự đặc biệt)
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }
        
        // Loại bỏ các ký tự không an toàn
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Lấy image URL hoặc default image nếu null
     */
    public static String getImageUrlOrDefault(String imageUrl, String defaultImage) {
        return (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : defaultImage;
    }
    
    /**
     * Lấy category image URL hoặc default
     */
    public static String getCategoryImageUrl(String imageUrl) {
        return getImageUrlOrDefault(imageUrl, DEFAULT_CATEGORY_IMAGE);
    }
    
    /**
     * Lấy product image URL hoặc default
     */
    public static String getProductImageUrl(String imageUrl) {
        return getImageUrlOrDefault(imageUrl, DEFAULT_PRODUCT_IMAGE);
    }
    
    /**
     * Lấy user avatar URL hoặc default
     */
    public static String getUserAvatarUrl(String imageUrl) {
        return getImageUrlOrDefault(imageUrl, DEFAULT_USER_AVATAR);
    }
    
    /**
     * Format file size từ bytes sang readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.1f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}