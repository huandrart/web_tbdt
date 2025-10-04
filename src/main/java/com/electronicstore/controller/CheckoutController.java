package com.electronicstore.controller;

import com.electronicstore.dto.CartItem;
import com.electronicstore.entity.*;
import com.electronicstore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String showCheckout(HttpSession session, Model model) {
        List<CartItem> cartItems = CartController.getCartItems(session);
        
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Calculate totals
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shippingFee = BigDecimal.valueOf(30000); // 30,000 VND
        BigDecimal total = subtotal.add(shippingFee);
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                model.addAttribute("user", userOpt.get());
            }
        }
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        model.addAttribute("paymentMethods", Order.PaymentMethod.values());
        
        return "checkout/checkout";
    }
    
    @PostMapping("/process")
    public String processCheckout(@RequestParam("customerName") String customerName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam("paymentMethod") Order.PaymentMethod paymentMethod,
                                 @RequestParam(value = "notes", required = false) String notes,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        List<CartItem> cartItems = CartController.getCartItems(session);
        
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống!");
            return "redirect:/cart";
        }
        
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                }
            }
            
            // Convert CartItems to OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                Product product = productService.findById(cartItem.getProductId());
                if (product != null) {
                    
                    // Check stock availability
                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Sản phẩm " + product.getName() + " không đủ hàng!");
                        return "redirect:/checkout";
                    }
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getUnitPrice());
                    orderItem.setTotalPrice(cartItem.getTotalPrice());
                    orderItems.add(orderItem);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Sản phẩm không tồn tại!");
                    return "redirect:/checkout";
                }
            }
            
            // Create order
            Order order = orderService.createOrder(user, orderItems, address, phone, customerName, notes);
            order.setPaymentMethod(paymentMethod);
            orderService.save(order);
            
            // Clear cart
            CartController.clearCart(session);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber());
            
            return "redirect:/checkout/success/" + order.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
    
    @GetMapping("/success/{orderId}")
    public String checkoutSuccess(@PathVariable Long orderId, Model model) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            return "checkout/success";
        } else {
            return "redirect:/";
        }
    }
}