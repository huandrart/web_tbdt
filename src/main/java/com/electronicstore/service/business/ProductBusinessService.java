package com.electronicstore.service.business;

import com.electronicstore.dto.request.ProductRequest;
import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.dto.response.ProductResponse;
import com.electronicstore.entity.Category;
import com.electronicstore.entity.Product;
import com.electronicstore.mapper.CategoryMapper;
import com.electronicstore.mapper.ProductMapper;
import com.electronicstore.service.CategoryService;
import com.electronicstore.service.FileStorageService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service layer for Product management
 * Handles business logic between Controller and Service layers
 */
@Service
public class ProductBusinessService {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    /**
     * Get paginated product list with filters
     */
    public ProductListViewModel getProductList(String search, String sortBy, String sortDir, 
                                               int page, int size, String status, Long categoryId) {
        
        // Create pageable object
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get paginated products
        Page<Product> productPage;
        if (StringUtils.hasText(search)) {
            productPage = productService.searchProducts(search, pageable);
        } else if (categoryId != null) {
            productPage = productService.findByCategory(categoryId, pageable);
        } else if ("active".equals(status)) {
            productPage = productService.searchProducts(null, null, true, pageable);
        } else if ("inactive".equals(status)) {
            productPage = productService.searchProducts(null, null, false, pageable);
        } else {
            productPage = productService.findAll(pageable);
        }
        
        // Convert to DTOs
        Page<ProductResponse> productResponsePage = productPage.map(productMapper::toResponse);
        
        // Create view model
        ProductListViewModel viewModel = new ProductListViewModel(productResponsePage);
        
        // Set current parameters
        viewModel.setCurrentSearch(search);
        viewModel.setCurrentSort(sortBy != null ? sortBy : "createdAt");
        viewModel.setCurrentDirection(sortDir != null ? sortDir : "desc");
        viewModel.setCurrentStatus(status);
        viewModel.setCurrentCategoryId(categoryId);
        
        // Calculate pagination
        calculatePaginationInfo(viewModel);
        
        return viewModel;
    }
    
    /**
     * Save product (create or update)
     */
    public ProductResponse saveProduct(ProductRequest request) {
        Product product;
        
        if (request.getId() != null) {
            // Update existing product
            product = productService.findById(request.getId());
            if (product == null) {
                throw new RuntimeException("Product not found with id: " + request.getId());
            }
            productMapper.updateEntity(product, request);
        } else {
            // Create new product
            product = productMapper.toEntity(request);
        }
        
        // Set category
        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("Category not found with id: " + request.getCategoryId());
            }
            product.setCategory(category);
        }
        
        // Handle image upload
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            try {
                String imageUrl = fileStorageService.saveProductImage(request.getImageFile());
                product.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save product image: " + e.getMessage());
            }
        }
        
        // Save product
        Product savedProduct = productService.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    /**
     * Create form view model for product creation
     */
    public ProductFormViewModel createNewProductForm() {
        ProductFormViewModel viewModel = new ProductFormViewModel();
        viewModel.setMode("create");
        viewModel.setTitle("Thêm sản phẩm mới");
        viewModel.setAction("/admin/products");
        viewModel.setMethod("POST");
        
        // Set default values
        ProductRequest productRequest = new ProductRequest();
        productRequest.setActive(true);
        productRequest.setFeatured(false);
        viewModel.setProduct(productRequest);
        
        // Load categories and convert to DTOs
        List<Category> categories = categoryService.findActiveCategories();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setCategories(categoryResponses);
        
        return viewModel;
    }
    
    /**
     * Create form view model for product editing
     */
    public ProductFormViewModel createEditProductForm(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        ProductFormViewModel viewModel = new ProductFormViewModel();
        viewModel.setMode("edit");
        viewModel.setTitle("Chỉnh sửa sản phẩm");
        viewModel.setAction("/admin/products/" + productId);
        viewModel.setMethod("PUT");
        
        // Convert entity to request DTO
        ProductRequest productRequest = productMapper.toRequest(product);
        viewModel.setProduct(productRequest);
        
        // Load categories and convert to DTOs
        List<Category> categories = categoryService.findActiveCategories();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        viewModel.setCategories(categoryResponses);
        
        return viewModel;
    }
    
    /**
     * Delete product (soft delete by setting active = false)
     */
    public void deleteProduct(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        productService.deleteById(productId);
    }
    
    /**
     * Toggle product active status
     */
    public ProductResponse toggleProductStatus(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        product.setIsActive(!product.getIsActive());
        Product savedProduct = productService.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    /**
     * Get product by ID
     */
    public ProductResponse getProductById(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        return productMapper.toResponse(product);
    }
    
    /**
     * Calculate pagination information
     */
    private void calculatePaginationInfo(ProductListViewModel viewModel) {
        int currentPage = viewModel.getCurrentPage();
        int totalPages = viewModel.getTotalPages();
        
        if (totalPages > 0) {
            List<Integer> pageNumbers = new ArrayList<>();
            
            int start = Math.max(0, currentPage - 2);
            int end = Math.min(totalPages - 1, currentPage + 2);
            
            // Always show first page
            if (start > 0) {
                pageNumbers.add(0);
                if (start > 1) {
                    pageNumbers.add(-1); // Ellipsis marker
                }
            }
            
            // Show pages around current page
            for (int i = start; i <= end; i++) {
                pageNumbers.add(i);
            }
            
            // Always show last page
            if (end < totalPages - 1) {
                if (end < totalPages - 2) {
                    pageNumbers.add(-1); // Ellipsis marker
                }
                pageNumbers.add(totalPages - 1);
            }
            
            viewModel.setPageNumbers(pageNumbers);
        }
    }
}