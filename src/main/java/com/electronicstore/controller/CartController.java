package com.electronicstore.controller;

import com.electronicstore.dto.CartItem;
import com.electronicstore.entity.Product;
import com.electronicstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private static final String CART_SESSION_KEY = "cart";
    
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCartFromSession(session);
        BigDecimal total = calculateTotal(cart);
        
        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartSize", cart.size());
        
        return "cart/cart";
    }
    
    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestParam("productId") Long productId,
                           @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                           HttpSession session) {
        
        try {
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
            
            List<CartItem> cart = getCartFromSession(session);
            
            // Check if product already in cart
            boolean found = false;
            for (CartItem item : cart) {
                if (item.getProductId().equals(productId)) {
                    int newQuantity = item.getQuantity() + quantity;
                    if (newQuantity > product.getStockQuantity()) {
                        return "{\"success\": false, \"message\": \"Số lượng vượt quá tồn kho! Tồn kho: " + product.getStockQuantity() + "\"}";
                    }
                    item.setQuantity(newQuantity);
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                CartItem cartItem = new CartItem();
                cartItem.setProductId(product.getId());
                cartItem.setProductName(product.getName());
                cartItem.setUnitPrice(product.getCurrentPrice());
                cartItem.setQuantity(quantity);
                cartItem.setTotalPrice(product.getCurrentPrice().multiply(BigDecimal.valueOf(quantity)));
                cartItem.setImageUrl(product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0));
                cart.add(cartItem);
            }
            
            session.setAttribute(CART_SESSION_KEY, cart);
            return "{\"success\": true, \"message\": \"Đã thêm vào giỏ hàng!\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/update")
    public String updateCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        if (quantity <= 0) {
            return removeFromCart(productId, session, redirectAttributes);
        }
        
        Product product = productService.findById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại!");
            return "redirect:/cart";
        }
        
        if (quantity > product.getStockQuantity()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Số lượng vượt quá tồn kho! Tồn kho: " + product.getStockQuantity());
            return "redirect:/cart";
        }
        
        List<CartItem> cart = getCartFromSession(session);
        
        for (CartItem item : cart) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                break;
            }
        }
        
        session.setAttribute(CART_SESSION_KEY, cart);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng!");
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getProductId().equals(productId));
        
        session.setAttribute(CART_SESSION_KEY, cart);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khỏi giỏ hàng!");
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(CART_SESSION_KEY);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng!");
        return "redirect:/cart";
    }
    
    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }
    
    // Helper methods
    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj instanceof List) {
            return (List<CartItem>) cartObj;
        }
        return new ArrayList<>();
    }
    
    private BigDecimal calculateTotal(List<CartItem> cart) {
        return cart.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @SuppressWarnings("unchecked")
    public static List<CartItem> getCartItems(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj instanceof List) {
            return (List<CartItem>) cartObj;
        }
        return new ArrayList<>();
    }
    
    public static void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}