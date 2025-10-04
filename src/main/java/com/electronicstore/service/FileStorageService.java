package com.electronicstore.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String categoryUploadDir = "uploads/categories/";
    private final String productUploadDir = "uploads/products/";
    private final String userUploadDir = "uploads/users/";

    public String saveProductImage(MultipartFile file) {
        return saveImage(file, productUploadDir);
    }

    public String saveCategoryImage(MultipartFile file) {
        return saveImage(file, categoryUploadDir);
    }

    public String saveUserImage(MultipartFile file) {
        return saveImage(file, userUploadDir);
    }

    public String storeFile(MultipartFile file) {
        return saveImage(file, productUploadDir);
    }

    private String saveImage(MultipartFile file, String targetDir) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(targetDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for web access
            return "/" + targetDir + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            if (fileName != null && fileName.startsWith("/uploads/")) {
                // Bỏ "/" ở đầu để có được path tương đối
                String relativePath = fileName.substring(1);
                Path filePath = Paths.get(relativePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + fileName);
        }
    }

    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // If already a full URL, return as is
        if (fileName.startsWith("http") || fileName.startsWith("/")) {
            return fileName;
        }
        // Otherwise, prepend with uploads directory
        return "/uploads/products/" + fileName;
    }
}