package com.electronicstore.repository;

import com.electronicstore.entity.Review;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProduct(Product product);
    
    List<Review> findByUser(User user);
    
    List<Review> findByProductAndIsApproved(Product product, Boolean isApproved);
    
    List<Review> findByIsApprovedFalse();
    
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.isApproved = true")
    Double getAverageRatingByProduct(@Param("product") Product product);
    
    long countByProductAndIsApproved(Product product, Boolean isApproved);
    
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByProduct(@Param("product") Product product);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.isApproved = false")
    long countPendingReviews();
}