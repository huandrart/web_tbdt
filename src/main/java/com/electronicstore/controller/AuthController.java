package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                              @RequestParam("confirmPassword") String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra mật khẩu xác nhận
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/register";
            }
            
            // Kiểm tra độ dài mật khẩu
            if (user.getPassword().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
                return "redirect:/register";
            }
            
            // Tạo user mới
            userService.createUser(user);
            
            return "redirect:/email-verification";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }
    
    @GetMapping("/email-verification")
    public String emailVerificationPage() {
        return "auth/email-verification";
    }
    
    @PostMapping("/forgot-password")
    public String sendResetCode(@RequestParam String email, 
                               RedirectAttributes redirectAttributes) {
        try {
            // Sử dụng UserService để tạo reset token và gửi email
            userService.generatePasswordResetToken(email);
            
            redirectAttributes.addFlashAttribute("success", 
                "Email đặt lại mật khẩu đã được gửi đến địa chỉ email của bạn!");
            
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, 
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra token có hợp lệ không
            if (!userService.validateResetToken(token)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn!");
                return "redirect:/forgot-password";
            }
            
            model.addAttribute("token", token);
            return "auth/reset-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra mật khẩu xác nhận
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu xác nhận không khớp!");
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/reset-password?token=" + token;
            }
            
            // Kiểm tra độ dài mật khẩu
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu phải có ít nhất 6 ký tự!");
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/reset-password?token=" + token;
            }
            
            // Sử dụng UserService để reset password
            userService.resetPassword(token, newPassword);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.");
            
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("token", token);
            return "redirect:/reset-password?token=" + token;
        }
    }
    
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, 
                             RedirectAttributes redirectAttributes) {
        try {
            boolean verified = userService.verifyEmail(token);
            
            if (verified) {
                redirectAttributes.addFlashAttribute("success", "Email đã được xác thực thành công! Bạn có thể đăng nhập ngay.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Liên kết xác thực không hợp lệ hoặc đã hết hạn!");
            }
            
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra trong quá trình xác thực: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @PostMapping("/resend-verification")
    public String resendVerificationEmail(@RequestParam("email") String email,
                                         RedirectAttributes redirectAttributes) {
        try {
            userService.resendVerificationEmail(email);
            redirectAttributes.addFlashAttribute("success", "Email xác thực đã được gửi lại! Vui lòng kiểm tra hộp thư.");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/login";
    }
}