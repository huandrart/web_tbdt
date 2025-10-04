package com.electronicstore.controller;

import com.electronicstore.entity.Product;
import com.electronicstore.entity.Category;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.CategoryService;
import com.electronicstore.dto.response.ProductResponse;
import com.electronicstore.dto.request.ProductRequest;
import com.electronicstore.mapper.ProductMapper;
import com.electronicstore.mapper.CategoryMapper;
import com.electronicstore.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {
        System.out.println("=== AdminProductController.listProducts START ===");
        System.out.println("Parameters: search=" + search + ", sortBy=" + sortBy + ", sortDir=" + sortDir + 
                          ", page=" + page + ", size=" + size + ", categoryId=" + categoryId + ", status=" + status);
        
        try {
            // Create pageable object
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get paginated products
            Page<Product> productPage;
            if (search != null && !search.trim().isEmpty()) {
                System.out.println("Searching products with keyword: " + search);
                productPage = productService.searchProducts(search, pageable);
            } else if (categoryId != null) {
                System.out.println("Filtering products by category: " + categoryId);
                productPage = productService.findByCategory(categoryId, pageable);
            } else if ("active".equals(status)) {
                System.out.println("Filtering active products");
                productPage = productService.searchProducts(null, null, true, pageable);
            } else if ("inactive".equals(status)) {
                System.out.println("Filtering inactive products");
                productPage = productService.searchProducts(null, null, false, pageable);
            } else {
                System.out.println("Getting all products");
                productPage = productService.findAll(pageable);
            }
            
            System.out.println("Found " + productPage.getTotalElements() + " products");
            
            // Convert to DTOs
            Page<ProductResponse> productResponsePage = productPage.map(product -> {
                ProductResponse response = new ProductResponse();
                response.setId(product.getId());
                response.setName(product.getName());
                response.setDescription(product.getDescription());
                response.setPrice(product.getPrice());
                response.setSalePrice(product.getSalePrice());
                response.setStock(product.getStockQuantity());
                response.setBrand(product.getBrand());
                response.setModel(product.getModel());
                response.setActive(product.getIsActive());
                response.setFeatured(product.getIsFeatured());
                response.setCreatedAt(product.getCreatedAt());
                response.setUpdatedAt(product.getUpdatedAt());
                response.setViewCount(product.getViewCount());
                
                if (product.getCategory() != null) {
                    response.setCategoryId(product.getCategory().getId());
                    response.setCategoryName(product.getCategory().getName());
                }
                
                if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                    response.setImageUrl(product.getImageUrls().get(0));
                }
                
                return response;
            });
            
            // Set model attributes
            model.addAttribute("products", productResponsePage);
            model.addAttribute("search", search);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("status", status);
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reversedDirection", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productResponsePage.getTotalPages());
            model.addAttribute("totalItems", productResponsePage.getTotalElements());
            
            // Load categories
            List<Category> categories = categoryService.findActiveCategories();
            model.addAttribute("categories", categories);
            
            // Set statistics
            long productCount = productService.findAll().size();
            model.addAttribute("activeItems", productCount);
            model.addAttribute("featuredItems", 0);
            model.addAttribute("lowStockItems", 0);
            
            System.out.println("Successfully prepared model for admin/products/list");
            System.out.println("=== AdminProductController.listProducts END ===");
            
            return "admin/products/list";
            
        } catch (Exception e) {
            System.err.println("=== ERROR in AdminProductController.listProducts ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END ERROR ===");
            
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("products", Page.empty());
            model.addAttribute("categories", List.of());
            return "admin/products/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("categories", categoryService.findActiveCategories());
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        model.addAttribute("submitButtonText", "Thêm mới");
        model.addAttribute("isEditMode", false);
        return "admin/products/form";
    }

    @PostMapping("/new")
    public String createProduct(@ModelAttribute ProductRequest productRequest,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Creating new product: " + productRequest.getName());
            
            // Convert to entity
            Product product = new Product();
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            product.setSalePrice(productRequest.getSalePrice());
            product.setStockQuantity(productRequest.getStockQuantity());
            product.setBrand(productRequest.getBrand());
            product.setModel(productRequest.getModel());
            product.setIsFeatured(productRequest.getIsFeatured() != null ? productRequest.getIsFeatured() : false);
            product.setIsActive(productRequest.getIsActive() != null ? productRequest.getIsActive() : true);
            
            // Set category
            if (productRequest.getCategoryId() != null) {
                Category category = categoryService.findByIdOptional(productRequest.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            }
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                product.getImageUrls().add(fileName);
            }
            
            // Save product
            Product savedProduct = productService.save(product);
            System.out.println("Product created successfully with ID: " + savedProduct.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được thêm thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            model.addAttribute("product", productRequest);
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("pageTitle", "Thêm sản phẩm mới");
            model.addAttribute("submitButtonText", "Thêm mới");
            model.addAttribute("isEditMode", false);
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findByIdOptional(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            ProductResponse response = new ProductResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setDescription(product.getDescription());
            response.setPrice(product.getPrice());
            response.setSalePrice(product.getSalePrice());
            response.setStock(product.getStockQuantity());
            response.setBrand(product.getBrand());
            response.setModel(product.getModel());
            response.setActive(product.getIsActive());
            response.setFeatured(product.getIsFeatured());
            response.setCreatedAt(product.getCreatedAt());
            response.setUpdatedAt(product.getUpdatedAt());
            response.setViewCount(product.getViewCount());
            
            if (product.getCategory() != null) {
                response.setCategoryId(product.getCategory().getId());
                response.setCategoryName(product.getCategory().getName());
            }
            
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                response.setImageUrl(product.getImageUrls().get(0));
            }
            
            model.addAttribute("product", response);
            return "admin/products/view";
            
        } catch (Exception e) {
            System.err.println("Error viewing product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/products?error=Product not found";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findByIdOptional(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            ProductRequest productRequest = new ProductRequest();
            productRequest.setId(product.getId());
            productRequest.setName(product.getName());
            productRequest.setDescription(product.getDescription());
            productRequest.setPrice(product.getPrice());
            productRequest.setSalePrice(product.getSalePrice());
            productRequest.setStockQuantity(product.getStockQuantity());
            productRequest.setBrand(product.getBrand());
            productRequest.setModel(product.getModel());
            productRequest.setIsFeatured(product.getIsFeatured());
            productRequest.setIsActive(product.getIsActive());
            
            if (product.getCategory() != null) {
                productRequest.setCategoryId(product.getCategory().getId());
            }
            
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                productRequest.setCurrentImageUrl(product.getImageUrls().get(0));
            }
            
            model.addAttribute("product", productRequest);
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
            model.addAttribute("submitButtonText", "Cập nhật");
            model.addAttribute("isEditMode", true);
            
            return "admin/products/form";
            
        } catch (Exception e) {
            System.err.println("Error loading product for edit: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/products?error=Product not found";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                               @ModelAttribute ProductRequest productRequest,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model, RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.findByIdOptional(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            // Update fields
            existingProduct.setName(productRequest.getName());
            existingProduct.setDescription(productRequest.getDescription());
            existingProduct.setPrice(productRequest.getPrice());
            existingProduct.setSalePrice(productRequest.getSalePrice());
            existingProduct.setStockQuantity(productRequest.getStockQuantity());
            existingProduct.setBrand(productRequest.getBrand());
            existingProduct.setModel(productRequest.getModel());
            existingProduct.setIsFeatured(productRequest.getIsFeatured() != null ? productRequest.getIsFeatured() : false);
            existingProduct.setIsActive(productRequest.getIsActive() != null ? productRequest.getIsActive() : true);
            
            // Set category
            if (productRequest.getCategoryId() != null) {
                Category category = categoryService.findByIdOptional(productRequest.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                existingProduct.setCategory(category);
            }
            
            // Handle image update
            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image if exists
                if (!existingProduct.getImageUrls().isEmpty()) {
                    fileStorageService.deleteFile(existingProduct.getImageUrls().get(0));
                }
                // Add new image
                String fileName = fileStorageService.storeFile(imageFile);
                existingProduct.getImageUrls().clear();
                existingProduct.getImageUrls().add(fileName);
            }
            
            // Save updated product
            Product updatedProduct = productService.save(existingProduct);
            System.out.println("Product updated successfully: " + updatedProduct.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được cập nhật thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("product", productRequest);
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
            model.addAttribute("submitButtonText", "Cập nhật");
            model.addAttribute("isEditMode", true);
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findByIdOptional(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            // Delete associated images
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                for (String imageUrl : product.getImageUrls()) {
                    fileStorageService.deleteFile(imageUrl);
                }
            }
            
            productService.deleteById(id);
            System.out.println("Product deleted successfully: " + id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findByIdOptional(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            product.setIsActive(!product.getIsActive());
            productService.save(product);
            
            String status = product.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được " + status + " thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            System.err.println("Error toggling product status: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thay đổi trạng thái sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }
}