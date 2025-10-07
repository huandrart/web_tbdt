package com.electronicstore.service.impl;

import com.electronicstore.entity.Review;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.repository.ReviewRepository;
import com.electronicstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }
    
    @Override
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }
    
    @Override
    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }
    
    @Override
    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user);
    }
    
    @Override
    public List<Review> findByProductAndIsApproved(Product product, Boolean isApproved) {
        return reviewRepository.findByProductAndIsApproved(product, isApproved);
    }
    
    @Override
    public Page<Review> findByProductAndIsApproved(Product product, Boolean isApproved, Pageable pageable) {
        return reviewRepository.findByProductAndIsApproved(product, isApproved, pageable);
    }
    
    @Override
    public List<Review> findPendingReviews() {
        return reviewRepository.findByIsApprovedFalse();
    }
    
    @Override
    public Optional<Review> findByUserAndProduct(User user, Product product) {
        return reviewRepository.findByUserAndProduct(user, product);
    }
    
    @Override
    public Double getAverageRatingByProduct(Product product) {
        return reviewRepository.getAverageRatingByProduct(product);
    }
    
    @Override
    public long countByProductAndIsApproved(Product product, Boolean isApproved) {
        return reviewRepository.countByProductAndIsApproved(product, isApproved);
    }
    
    @Override
    public long countPendingReviews() {
        return reviewRepository.countPendingReviews();
    }
    
    @Override
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }
    
    @Override
    public Review createReview(Product product, User user, Integer rating, String comment) {
        // Kiểm tra xem user đã review sản phẩm này chưa
        Optional<Review> existingReview = findByUserAndProduct(user, product);
        if (existingReview.isPresent()) {
            // Cập nhật review cũ
            Review review = existingReview.get();
            review.setRating(rating);
            review.setComment(comment);
            review.setIsApproved(false); // Cần duyệt lại
            return save(review);
        } else {
            // Tạo review mới
            Review review = new Review(product, user, rating, comment);
            return save(review);
        }
    }
    
    @Override
    public boolean canUserReview(User user, Product product) {
        // User có thể review nếu chưa review sản phẩm này
        return !findByUserAndProduct(user, product).isPresent();
    }
}
