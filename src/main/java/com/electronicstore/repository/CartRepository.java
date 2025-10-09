package com.electronicstore.repository;

import com.electronicstore.entity.Cart;
import com.electronicstore.entity.User;
import com.electronicstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    
    List<Cart> findByUser(User user);
    
    Optional<Cart> findByUserAndProduct(User user, Product product);
    
    void deleteByUserAndProduct(User user, Product product);
    
    void deleteByUser(User user);
    
    long countByUser(User user);
    
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM Cart c WHERE c.user = :user")
    Long sumQuantityByUser(@Param("user") User user);
}
