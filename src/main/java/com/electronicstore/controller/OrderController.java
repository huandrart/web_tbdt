package com.electronicstore.controller;

import com.electronicstore.dto.CartItem;
import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {
    
    private static final String CART_SESSION_KEY = "cart";
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/order")
    public String orderPage(Authentication authentication, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Lấy thông tin user
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Lấy giỏ hàng từ session
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Tính toán tổng tiền
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shippingFee = BigDecimal.ZERO; // Miễn phí ship
        BigDecimal taxRate = new BigDecimal("0.1"); // 10% VAT
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal total = subtotal.add(shippingFee).add(tax);
        
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        
        return "order/place-order";
    }
    
    @PostMapping("/order/place")
    public String placeOrder(@RequestParam("paymentMethod") String paymentMethod,
                           Authentication authentication,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        // Kiểm tra đăng nhập
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Lấy thông tin user
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Kiểm tra thông tin địa chỉ
        if (user.getAddress() == null || user.getAddress().trim().isEmpty() ||
            user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Vui lòng cập nhật thông tin địa chỉ và số điện thoại trước khi đặt hàng!");
            return "redirect:/profile/edit";
        }
        
        // Kiểm tra phương thức thanh toán
        if ("BANK_TRANSFER".equals(paymentMethod)) {
            // Kiểm tra thẻ ngân hàng (tạm thời chuyển đến trang profile)
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Vui lòng thêm thông tin thẻ ngân hàng trước khi sử dụng chuyển khoản!");
            return "redirect:/profile/edit";
        }
        
        // TODO: Tạo đơn hàng và lưu vào database
        // TODO: Xóa giỏ hàng sau khi đặt hàng thành công
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đặt hàng thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất.");
        
        return "redirect:/orders/my-orders";
    }
}
