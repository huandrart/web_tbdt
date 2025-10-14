package com.electronicstore.service;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.ShipperReview;
import com.electronicstore.entity.User;
import com.electronicstore.repository.ShipperReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShipperReviewService {
    
    @Autowired
    private ShipperReviewRepository shipperReviewRepository;
    
    public ShipperReview save(ShipperReview shipperReview) {
        return shipperReviewRepository.save(shipperReview);
    }
    
    public Optional<ShipperReview> findByOrder(Order order) {
        return shipperReviewRepository.findByOrder(order);
    }
    
    public List<ShipperReview> findByShipper(User shipper) {
        return shipperReviewRepository.findByShipper(shipper);
    }
    
    public List<ShipperReview> findByCustomer(User customer) {
        return shipperReviewRepository.findByCustomer(customer);
    }
    
    public Double getAverageRatingByShipper(User shipper) {
        Double average = shipperReviewRepository.findAverageRatingByShipper(shipper);
        return average != null ? average : 0.0;
    }
    
    public Long getReviewCountByShipper(User shipper) {
        return shipperReviewRepository.countByShipper(shipper);
    }
    
    public List<ShipperReview> findByShipperOrderByCreatedAtDesc(User shipper) {
        return shipperReviewRepository.findByShipperOrderByCreatedAtDesc(shipper);
    }
    
    public boolean hasReviewedOrder(Order order) {
        return shipperReviewRepository.findByOrder(order).isPresent();
    }
    
    public boolean canReviewOrder(Order order, User customer) {
        // Customer can review if:
        // 1. Order belongs to the customer
        // 2. Order status is DELIVERED
        // 3. Order has not been reviewed yet
        return order.getUser().equals(customer) && 
               order.getStatus() == Order.OrderStatus.DELIVERED &&
               !hasReviewedOrder(order);
    }
}
