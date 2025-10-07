package com.electronicstore.controller;

import com.electronicstore.dto.CartItem;
import com.electronicstore.entity.Cart;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.service.CartService;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Get user from authentication
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Get cart from database
        List<Cart> cartItems = cartService.findByUser(user);
        List<CartItem> cartItemDTOs = convertToCartItemDTOs(cartItems);
        BigDecimal total = calculateTotal(cartItemDTOs);
        
        model.addAttribute("cartItems", cartItemDTOs);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartSize", cartItemDTOs.size());
        
        return "cart/cart";
    }
    
    // Convert Cart entities to CartItem DTOs
    private List<CartItem> convertToCartItemDTOs(List<Cart> cartItems) {
        List<CartItem> cartItemDTOs = new ArrayList<>();
        
        for (Cart cart : cartItems) {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(cart.getProduct().getId());
            cartItem.setProductName(cart.getProduct().getName());
            cartItem.setUnitPrice(cart.getProduct().getCurrentPrice());
            cartItem.setQuantity(cart.getQuantity());
            cartItem.setTotalPrice(cart.getProduct().getCurrentPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            cartItem.setImageUrl(cart.getProduct().getImageUrls() != null && !cart.getProduct().getImageUrls().isEmpty() 
                ? cart.getProduct().getImageUrls().get(0) : "no-image.png");
            
            cartItemDTOs.add(cartItem);
        }
        
        return cartItemDTOs;
    }
    
    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestParam("productId") Long productId,
                           @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                           Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "{\"success\": false, \"message\": \"Bạn cần đăng nhập để thêm vào giỏ hàng!\"}";
            }
            
            // Get user
            String userEmail = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return "{\"success\": false, \"message\": \"Không tìm thấy thông tin người dùng!\"}";
            }
            
            User user = userOpt.get();
            
            Product product = productService.findById(productId);
            if (product == null) {
                return "{\"success\": false, \"message\": \"Sản phẩm không tồn tại!\"}";
            }
            
            if (!product.getIsActive()) {
                return "{\"success\": false, \"message\": \"Sản phẩm không còn bán!\"}";
            }
            
            if (product.getStockQuantity() < quantity) {
                return "{\"success\": false, \"message\": \"Không đủ hàng trong kho!\"}";
            }
            
            // Add to cart in database
            cartService.addToCart(user, product, quantity);
            
            return "{\"success\": true, \"message\": \"Đã thêm vào giỏ hàng!\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/update")
    @ResponseBody
    public String updateCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") Integer quantity,
                            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "{\"success\": false, \"message\": \"Bạn cần đăng nhập!\"}";
            }
            
            // Get user
            String userEmail = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return "{\"success\": false, \"message\": \"Không tìm thấy thông tin người dùng!\"}";
            }
            
            User user = userOpt.get();
            
            if (quantity <= 0) {
                return removeFromCart(productId, authentication);
            }
            
            Product product = productService.findById(productId);
            if (product == null) {
                return "{\"success\": false, \"message\": \"Sản phẩm không tồn tại!\"}";
            }
            
            if (quantity > product.getStockQuantity()) {
                return "{\"success\": false, \"message\": \"Số lượng vượt quá tồn kho! Tồn kho: " + product.getStockQuantity() + "\"}";
            }
            
            // Update cart in database
            cartService.updateQuantity(user, product, quantity);
            
            return "{\"success\": true, \"message\": \"Đã cập nhật số lượng!\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/remove")
    @ResponseBody
    public String removeFromCart(@RequestParam("productId") Long productId,
                                Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "{\"success\": false, \"message\": \"Bạn cần đăng nhập!\"}";
            }
            
            // Get user
            String userEmail = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return "{\"success\": false, \"message\": \"Không tìm thấy thông tin người dùng!\"}";
            }
            
            User user = userOpt.get();
            
            Product product = productService.findById(productId);
            if (product == null) {
                return "{\"success\": false, \"message\": \"Sản phẩm không tồn tại!\"}";
            }
            
            // Remove from cart in database
            cartService.removeFromCart(user, product);
            
            return "{\"success\": true, \"message\": \"Đã xóa sản phẩm khỏi giỏ hàng!\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/clear")
    @ResponseBody
    public String clearCart(Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "{\"success\": false, \"message\": \"Bạn cần đăng nhập!\"}";
            }
            
            // Get user
            String userEmail = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return "{\"success\": false, \"message\": \"Không tìm thấy thông tin người dùng!\"}";
            }
            
            User user = userOpt.get();
            
            // Clear cart in database
            cartService.clearCart(user);
            
            return "{\"success\": true, \"message\": \"Đã xóa tất cả sản phẩm khỏi giỏ hàng!\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
    
    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return 0;
        }
        
        User user = userOpt.get();
        Long count = cartService.sumQuantityByUser(user);
        return count != null ? count.intValue() : 0;
    }
    
    private BigDecimal calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<Product> getSuggestedProducts() {
        // Return some suggested products
        return productService.findAll().stream()
                .filter(Product::getIsActive)
                .limit(3)
                .toList();
    }
}
