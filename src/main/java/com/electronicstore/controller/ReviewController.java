package com.electronicstore.controller;

import com.electronicstore.entity.Product;
import com.electronicstore.entity.Review;
import com.electronicstore.entity.User;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.ReviewService;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/add")
    @ResponseBody
    public String addReview(@RequestParam("productId") Long productId,
                           @RequestParam("rating") Integer rating,
                           @RequestParam("comment") String comment,
                           Authentication authentication) {
        try {
            System.out.println("DEBUG: ReviewController.addReview called");
            System.out.println("DEBUG: productId=" + productId + ", rating=" + rating + ", comment=" + comment);
            
            // Kiểm tra đăng nhập
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("DEBUG: User not authenticated");
                return "{\"success\": false, \"message\": \"Bạn cần đăng nhập để đánh giá!\"}";
            }
            
            // Lấy thông tin user
            String userEmail = authentication.getName();
            System.out.println("DEBUG: userEmail=" + userEmail);
            User user = userService.findByEmail(userEmail).orElse(null);
            if (user == null) {
                System.out.println("DEBUG: User not found");
                return "{\"success\": false, \"message\": \"Không tìm thấy thông tin người dùng!\"}";
            }
            
            // Lấy thông tin sản phẩm
            Product product = productService.findById(productId);
            if (product == null) {
                System.out.println("DEBUG: Product not found");
                return "{\"success\": false, \"message\": \"Sản phẩm không tồn tại!\"}";
            }
            
            // Kiểm tra rating hợp lệ
            if (rating < 1 || rating > 5) {
                System.out.println("DEBUG: Invalid rating");
                return "{\"success\": false, \"message\": \"Đánh giá phải từ 1 đến 5 sao!\"}";
            }
            
            // Tạo review
            System.out.println("DEBUG: Creating review...");
            Review review = reviewService.createReview(product, user, rating, comment);
            System.out.println("DEBUG: Review created with ID: " + review.getId());
            
            return "{\"success\": true, \"message\": \"Đánh giá đã được gửi thành công! Cảm ơn bạn đã chia sẻ trải nghiệm.\"}";
            
        } catch (Exception e) {
            System.out.println("DEBUG: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
}
