package com.electronicstore.controller;

import com.electronicstore.entity.Category;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.Review;
import com.electronicstore.service.CategoryService;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Lấy sản phẩm nổi bật
        Pageable featuredPageable = PageRequest.of(0, 8);
        List<Product> featuredProducts = productService.findFeaturedProductsWithPagination(featuredPageable).getContent();
        
        // Lấy sản phẩm phổ biến
        Pageable popularPageable = PageRequest.of(0, 8);
        List<Product> popularProducts = productService.findPopularProducts(popularPageable);
        
        // Lấy danh mục
        List<Category> categories = categoryService.findActiveCategoriesOrderByName();
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("popularProducts", popularProducts);
        model.addAttribute("categories", categories);
        
        return "index";
    }
    
    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProducts(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else if (categoryId != null) {
            Category category = categoryService.findById(categoryId);
            if (category != null) {
                if (minPrice != null && maxPrice != null) {
                    productPage = productService.findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable);
                } else {
                    productPage = productService.findByCategoryAndIsActiveTrue(category, pageable);
                }
                model.addAttribute("selectedCategory", category);
                model.addAttribute("categoryId", categoryId);
            } else {
                productPage = productService.findActiveProductsWithPagination(pageable);
            }
        } else if (minPrice != null && maxPrice != null) {
            productPage = productService.findByPriceRange(minPrice, maxPrice, pageable);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
        } else {
            productPage = productService.findActiveProductsWithPagination(pageable);
        }
        
        // Lấy danh sách danh mục và thương hiệu cho filter
        List<Category> categories = categoryService.findActiveCategoriesOrderByName();
        List<String> brands = productService.findAllBrands();
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        
        return "products";
    }
    
        @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/";
        }
        
        // Tăng view count
        productService.incrementViewCount(id);
        
        // Lấy sản phẩm liên quan (cùng danh mục)
        if (product.getCategory() != null) {
            PageRequest pageRequest = PageRequest.of(0, 4);
            Page<Product> relatedProducts = productService.findByCategoryAndIsActiveTrue(
                product.getCategory(), pageRequest);
            model.addAttribute("relatedProducts", relatedProducts.getContent());
        }
        
        // Lấy tất cả reviews (bao gồm cả chưa duyệt để testing)
        List<Review> allReviews = reviewService.findByProduct(product);
        Double averageRating = reviewService.getAverageRatingByProduct(product);
        long reviewCount = allReviews.size();
        
        model.addAttribute("product", product);
        model.addAttribute("reviews", allReviews);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("reviewCount", reviewCount);
        return "product/detail";
    }
    
    @GetMapping("/category/{id}")  
    public String categoryPage(@PathVariable Long id, Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findByCategoryAndIsActiveTrue(category, pageable);
        
        model.addAttribute("category", category);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        
        return "category-products";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}