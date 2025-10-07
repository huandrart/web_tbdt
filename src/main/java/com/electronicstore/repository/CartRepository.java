package com.electronicstore.repository;

import com.electronicstore.entity.Cart;
import com.electronicstore.entity.User;
import com.electronicstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    List<Cart> findByUser(User user);
    
    Optional<Cart> findByUserAndProduct(User user, Product product);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
    
    @Modifying
    @Transactional
    void deleteByUserAndProduct(User user, Product product);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT SUM(c.quantity) FROM Cart c WHERE c.user = :user")
    Long sumQuantityByUser(@Param("user") User user);
}
