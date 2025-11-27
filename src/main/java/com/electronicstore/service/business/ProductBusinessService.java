package com.electronicstore.service.business;

import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.dto.response.ProductResponse;
import com.electronicstore.entity.Category;
import com.electronicstore.entity.Product;
import com.electronicstore.mapper.CategoryMapper;
import com.electronicstore.mapper.ProductMapper;
import com.electronicstore.service.CategoryService;
import com.electronicstore.service.ProductService;
import com.electronicstore.viewmodel.ProductFormViewModel;
import com.electronicstore.viewmodel.ProductListViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service for product management
 */
@Service
public class ProductBusinessService {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    /**
     * Get paginated product list with filters
     */
    public ProductListViewModel getProductList(String search, String sortBy, String sortDir, 
                                               int page, int size, String status, Long categoryId) {
        
        System.out.println("ProductBusinessService.getProductList called with: search=" + search + 
                          ", sortBy=" + sortBy + ", sortDir=" + sortDir + ", page=" + page + 
                          ", size=" + size + ", status=" + status + ", categoryId=" + categoryId);
        
        // --- SỬA TỪ ĐÂY ---

            // Mặc định ban đầu
            Sort.Direction direction = Sort.Direction.DESC;
            String property = "createdAt";

            if (StringUtils.hasText(sortBy)) {
                // Trường hợp 1: URL gửi lên dạng "name-asc", "price-desc" (có dấu gạch ngang)
                if (sortBy.contains("-")) {
                    String[] parts = sortBy.split("-");
                    property = parts[0]; // Lấy phần "name" hoặc "price"
                    
                    // Kiểm tra chiều sắp xếp
                    if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
                        direction = Sort.Direction.ASC;
                    } else {
                        direction = Sort.Direction.DESC;
                    }
                } 
                // Trường hợp 2: URL gửi lên dạng "name_asc" (nếu frontend dùng dấu gạch dưới)
                else if (sortBy.contains("_")) {
                    String[] parts = sortBy.split("_");
                    property = parts[0];
                    if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
                        direction = Sort.Direction.ASC;
                    } else {
                        direction = Sort.Direction.DESC;
                    }
                }
                // Trường hợp 3: Chỉ gửi tên trường (ít gặp với frontend của bạn)
                else {
                    property = sortBy;
                    direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
                }
            }

            // Xử lý map tên đặc biệt (Nếu frontend gửi "newest" thay vì "createdAt-desc")
            if ("newest".equalsIgnoreCase(property)) {
                property = "createdAt";
                direction = Sort.Direction.DESC;
            }

            Sort sort = Sort.by(direction, property);
            Pageable pageable = PageRequest.of(page, size, sort);

            // --- HẾT PHẦN SỬA ---
        
        // Get paginated products
        Page<Product> productPage;
        if (StringUtils.hasText(search)) {
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
        Page<ProductResponse> productResponsePage = productPage.map(productMapper::toResponse);
        
        // Create view model
        ProductListViewModel viewModel = new ProductListViewModel(productResponsePage);
        viewModel.setCurrentSearch(search);
        viewModel.setCurrentSort(sortBy);
        viewModel.setCurrentDirection(sortDir);
        viewModel.setCurrentStatus(status);
        viewModel.setCurrentCategoryId(categoryId);
        
        // Load categories for filter dropdown
        List<Category> categories = categoryService.findActiveCategories();
        viewModel.setCategories(categories);
        
        // Calculate statistics
        long activeItems = productService.findAll().stream()
                .mapToLong(p -> p.getIsActive() ? 1 : 0)
                .sum();
        long featuredItems = productService.findAll().stream()
                .mapToLong(p -> p.getIsFeatured() ? 1 : 0)
                .sum();
        long lowStockItems = productService.findAll().stream()
                .mapToLong(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5 ? 1 : 0)
                .sum();
        
        viewModel.setActiveItems(activeItems);
        viewModel.setFeaturedItems(featuredItems);
        viewModel.setLowStockItems(lowStockItems);
        
        return viewModel;
    }
    
    /**
     * Create new product form
     */
    public ProductFormViewModel createNewProductForm() {
        ProductFormViewModel viewModel = new ProductFormViewModel(false);
        viewModel.setTitle("Thêm sản phẩm mới");
        viewModel.setAction("/admin/products");
        viewModel.setMethod("POST");
        
        // Load categories and convert to DTOs
        List<Category> categories = categoryService.findActiveCategories();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setCategories(categoryResponses);
        
        return viewModel;
    }
    
    /**
     * Create edit product form
     */
    public ProductFormViewModel createEditProductForm(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }
        
        ProductFormViewModel viewModel = new ProductFormViewModel(true);
        viewModel.setTitle("Chỉnh sửa sản phẩm");
        viewModel.setAction("/admin/products/" + productId);
        viewModel.setMethod("PUT");
        
        // Load categories and convert to DTOs
        List<Category> categories = categoryService.findActiveCategories();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setCategories(categoryResponses);
        
        return viewModel;
    }
    
    /**
     * Save product (create or update)
     */
    public ProductResponse saveProduct() {
        // Simple implementation - just return a dummy response
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Test Product");
        response.setDescription("Test Description");
        response.setPrice(java.math.BigDecimal.valueOf(100000));
        response.setStock(10);
        response.setActive(true);
        return response;
    }
    
    /**
     * Delete product
     */
    public void deleteProduct(Long productId) {
        productService.deleteById(productId);
    }
    
    /**
     * Toggle product status
     */
    public void toggleProductStatus(Long productId) {
        Product product = productService.findById(productId);
        if (product != null) {
            product.setIsActive(!product.getIsActive());
            productService.save(product);
        }
    }
}