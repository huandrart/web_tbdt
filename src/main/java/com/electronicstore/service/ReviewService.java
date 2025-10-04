package com.electronicstore.service;

import com.electronicstore.entity.Review;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }
    
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }
    
    public List<Review> findByProductAndApproved(Product product, boolean approved) {
        return reviewRepository.findByProductAndIsApproved(product, approved);
    }
    
    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user);
    }
    
    public List<Review> findPendingReviews() {
        return reviewRepository.findByIsApprovedFalse();
    }
    
    public Optional<Review> findByUserAndProduct(User user, Product product) {
        return reviewRepository.findByUserAndProduct(user, product);
    }
    
    public Review save(Review review) {
        return reviewRepository.save(review);
    }
    
    public Review addReview(User user, Product product, Integer rating, String comment) {
        // Check if user already reviewed this product
        if (reviewRepository.findByUserAndProduct(user, product).isPresent()) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }
        
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsApproved(false); // Require approval
        
        return reviewRepository.save(review);
    }
    
    public Review approveReview(Long reviewId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            review.setIsApproved(true);
            return reviewRepository.save(review);
        }
        throw new IllegalArgumentException("Đánh giá không tồn tại");
    }
    
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }
    
    public Double getAverageRatingForProduct(Product product) {
        return reviewRepository.getAverageRatingByProduct(product);
    }
    
    public long countReviewsForProduct(Product product) {
        return reviewRepository.countByProductAndIsApproved(product, true);
    }
    
    public boolean hasUserReviewedProduct(User user, Product product) {
        return reviewRepository.findByUserAndProduct(user, product).isPresent();
    }
}