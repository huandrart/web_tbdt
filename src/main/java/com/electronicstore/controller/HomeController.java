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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;

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
        
        // Lấy danh mục có sản phẩm
        List<Category> categories = categoryService.findActiveCategoriesWithProducts();
        
        // Tính số sản phẩm cho mỗi danh mục
        for (Category category : categories) {
            long productCount = categoryService.countProductsByCategory(category);
            category.setProductCount(productCount);
        }
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("popularProducts", popularProducts);
        model.addAttribute("categories", categories);
        
        return "index";
    }
    
    @GetMapping("/home-page")
    public String homePage(Model model) {
        // Lấy sản phẩm nổi bật
        Pageable featuredPageable = PageRequest.of(0, 8);
        List<Product> featuredProducts = productService.findFeaturedProductsWithPagination(featuredPageable).getContent();
        
        // Lấy sản phẩm phổ biến
        Pageable popularPageable = PageRequest.of(0, 8);
        List<Product> popularProducts = productService.findPopularProducts(popularPageable);
        
        // Lấy danh mục có sản phẩm
        List<Category> categories = categoryService.findActiveCategoriesWithProducts();
        
        // Tính số sản phẩm cho mỗi danh mục
        for (Category category : categories) {
            long productCount = categoryService.countProductsByCategory(category);
            category.setProductCount(productCount);
        }
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("popularProducts", popularProducts);
        model.addAttribute("categories", categories);
        
        return "home";
    }
    
    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort, 
            @RequestParam(required = false) String keyword,
            Model model) {
        

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price-asc":
                    sorting = Sort.by("price").ascending();
                    break;
                case "price-desc":
                    sorting = Sort.by("price").descending(); // Sẽ chạy
                    break;
                case "name-asc":
                    // SỬA: Thay "name" bằng tên chính xác trong Entity (ví dụ "productName" hoặc giữ nguyên nếu đúng)
                    sorting = Sort.by("name").ascending(); 
                    break;
                case "name-desc":
                    sorting = Sort.by("name").descending();
                    break;
            }
        }

        System.out.println("2. Đối tượng Sort sau khi xử lý: " + sorting.toString());

        Pageable pageable = PageRequest.of(page, size, sorting);
           
            
        
      
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
        
        // Lấy danh sách danh mục có sản phẩm và thương hiệu cho filter
        List<Category> categories = categoryService.findActiveCategoriesWithProducts();
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
                              @RequestParam(defaultValue = "12") int size,
                              @RequestParam(required = false) String sort) { // <-- THÊM BIẾN SORT
        
        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/";
        }
        
        // 1. Xử lý Logic Sắp xếp
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price-asc":
                    sorting = Sort.by("price").ascending();
                    break;
                case "price-desc":
                    sorting = Sort.by("price").descending();
                    break;
                case "name-asc":
                    sorting = Sort.by("name").ascending();
                    break;
                case "name-desc":
                    sorting = Sort.by("name").descending();
                    break;
                case "newest":
                    // Lưu ý: Đảm bảo trong Entity Product có trường "createdAt" hoặc "id"
                    sorting = Sort.by("id").descending(); 
                    break;
                default:
                    sorting = Sort.unsorted();
                    break;
            }
        }

        // 2. Tạo PageRequest với thông tin sắp xếp
        Pageable pageable = PageRequest.of(page, size, sorting);
        
        // 3. Gọi Service lấy dữ liệu
        Page<Product> productPage = productService.findByCategoryAndIsActiveTrue(category, pageable);
        
        model.addAttribute("category", category);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("productCount", productPage.getTotalElements());
        
        // Trả lại giá trị sort để Frontend giữ trạng thái select box
        model.addAttribute("currentSort", sort); 
        
        return "category-products";
    }
    
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "Giới thiệu - Electronic Store");
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Liên hệ - Electronic Store");
        return "contact";
    }
    
    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String subject,
                               @RequestParam String message,
                               RedirectAttributes redirectAttributes) {
        try {
            // TODO: Gửi email thông báo cho admin
            // Tạm thời chỉ log thông tin
            System.out.println("Contact form submission:");
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + message);
            
            redirectAttributes.addFlashAttribute("success", 
                "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi gửi tin nhắn. Vui lòng thử lại sau.");
        }
        
        return "redirect:/contact";
    }
}