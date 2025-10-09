package com.electronicstore.service;

import com.electronicstore.entity.Cart;
import com.electronicstore.entity.User;
import com.electronicstore.entity.Product;

import java.util.List;
import java.util.Optional;

public interface CartService {
    
    List<Cart> findByUser(User user);
    
    Cart addToCart(User user, Product product, Integer quantity);
    
    Cart updateQuantity(User user, Product product, Integer quantity);
    
    void removeFromCart(User user, Product product);
    
    void clearCart(User user);
    
    Optional<Cart> findByUserAndProduct(User user, Product product);
    
    long countByUser(User user);
    
    Long sumQuantityByUser(User user);
}

