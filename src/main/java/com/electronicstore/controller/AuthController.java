package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String sendResetCode(@RequestParam String email, 
                               RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra email có tồn tại không
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Email không tồn tại trong hệ thống!");
                return "redirect:/auth/forgot-password";
            }
            
            User user = userOpt.get();
            
            // Kiểm tra tài khoản có bị khóa không
            if (!user.getIsActive()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên!");
                return "redirect:/auth/forgot-password";
            }
            
            // TODO: Gửi mã xác thực qua email
            // Tạm thời sử dụng mã cố định để test
            String resetCode = "123456";
            
            // Lưu mã reset vào session hoặc database
            // Ở đây tạm thời lưu vào session
            redirectAttributes.addFlashAttribute("resetCode", resetCode);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("success", 
                "Mã xác thực đã được gửi đến email của bạn!");
            
            return "redirect:/auth/forgot-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }
    
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email,
                           @RequestParam String code,
                           RedirectAttributes redirectAttributes) {
        try {
            // Lấy mã reset từ session
            String sessionCode = (String) redirectAttributes.getFlashAttributes().get("resetCode");
            
            // Kiểm tra mã xác thực
            if (sessionCode == null || !sessionCode.equals(code)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mã xác thực không đúng!");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/auth/forgot-password";
            }
            
            // Mã đúng, chuyển đến bước đặt mật khẩu mới
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("verified", true);
            redirectAttributes.addFlashAttribute("success", 
                "Mã xác thực đúng! Vui lòng đặt mật khẩu mới.");
            
            return "redirect:/auth/forgot-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra mật khẩu xác nhận
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu xác nhận không khớp!");
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("verified", true);
                return "redirect:/auth/forgot-password";
            }
            
            // Kiểm tra độ dài mật khẩu
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu phải có ít nhất 6 ký tự!");
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("verified", true);
                return "redirect:/auth/forgot-password";
            }
            
            // Tìm user và cập nhật mật khẩu
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Email không tồn tại!");
                return "redirect:/auth/forgot-password";
            }
            
            User user = userOpt.get();
            user.setPassword(newPassword); // UserService sẽ encode password
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("verified", true);
            return "redirect:/auth/forgot-password";
        }
    }
}