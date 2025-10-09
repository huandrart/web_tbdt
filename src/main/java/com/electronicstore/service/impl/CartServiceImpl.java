package com.electronicstore.service.impl;

import com.electronicstore.entity.Cart;
import com.electronicstore.entity.User;
import com.electronicstore.entity.Product;
import com.electronicstore.repository.CartRepository;
import com.electronicstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Override
    public List<Cart> findByUser(User user) {
        return cartRepository.findByUser(user);
    }
    
    @Override
    public Cart addToCart(User user, Product product, Integer quantity) {
        Optional<Cart> existingCart = cartRepository.findByUserAndProduct(user, product);
        
        if (existingCart.isPresent()) {
            // Update existing cart item
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            // Create new cart item
            Cart cart = new Cart(user, product, quantity);
            return cartRepository.save(cart);
        }
    }
    
    @Override
    public Cart updateQuantity(User user, Product product, Integer quantity) {
        Optional<Cart> existingCart = cartRepository.findByUserAndProduct(user, product);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        } else {
            // Create new cart item if not exists
            Cart cart = new Cart(user, product, quantity);
            return cartRepository.save(cart);
        }
    }
    
    @Override
    public void removeFromCart(User user, Product product) {
        cartRepository.deleteByUserAndProduct(user, product);
    }
    
    @Override
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }
    
    @Override
    public Optional<Cart> findByUserAndProduct(User user, Product product) {
        return cartRepository.findByUserAndProduct(user, product);
    }
    
    @Override
    public long countByUser(User user) {
        return cartRepository.countByUser(user);
    }
    
    @Override
    public Long sumQuantityByUser(User user) {
        Long sum = cartRepository.sumQuantityByUser(user);
        return sum != null ? sum : 0L;
    }
}

