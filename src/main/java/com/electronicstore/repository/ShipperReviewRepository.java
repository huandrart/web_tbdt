package com.electronicstore.repository;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.ShipperReview;
import com.electronicstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperReviewRepository extends JpaRepository<ShipperReview, Long> {
    
    Optional<ShipperReview> findByOrder(Order order);
    
    List<ShipperReview> findByShipper(User shipper);
    
    List<ShipperReview> findByCustomer(User customer);
    
    @Query("SELECT AVG(sr.rating) FROM ShipperReview sr WHERE sr.shipper = :shipper")
    Double findAverageRatingByShipper(@Param("shipper") User shipper);
    
    @Query("SELECT COUNT(sr) FROM ShipperReview sr WHERE sr.shipper = :shipper")
    Long countByShipper(@Param("shipper") User shipper);
    
    @Query("SELECT sr FROM ShipperReview sr WHERE sr.shipper = :shipper ORDER BY sr.createdAt DESC")
    List<ShipperReview> findByShipperOrderByCreatedAtDesc(@Param("shipper") User shipper);
}
