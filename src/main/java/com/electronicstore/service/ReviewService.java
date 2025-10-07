package com.electronicstore.service;

import com.electronicstore.entity.Review;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    
    Review save(Review review);
    
    Optional<Review> findById(Long id);
    
    List<Review> findByProduct(Product product);
    
    List<Review> findByUser(User user);
    
    List<Review> findByProductAndIsApproved(Product product, Boolean isApproved);
    
    Page<Review> findByProductAndIsApproved(Product product, Boolean isApproved, Pageable pageable);
    
    List<Review> findPendingReviews();
    
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    Double getAverageRatingByProduct(Product product);
    
    long countByProductAndIsApproved(Product product, Boolean isApproved);
    
    long countPendingReviews();
    
    void deleteById(Long id);
    
    Review createReview(Product product, User user, Integer rating, String comment);
    
    boolean canUserReview(User user, Product product);
}