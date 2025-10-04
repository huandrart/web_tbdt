package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Hiển thị thông tin profile
    @GetMapping
    public String showProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        return "profile/profile";
    }

    // Hiển thị form edit profile
    @GetMapping("/edit")
    public String showEditProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        return "profile/edit-profile";
    }

    // Xử lý update profile
    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Cập nhật thông tin (giữ nguyên email và password)
            currentUser.setFullName(user.getFullName());
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            
            // Đồng bộ thông tin name nếu cần
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                String[] nameParts = user.getFullName().trim().split("\\s+");
                if (nameParts.length >= 2) {
                    if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                        currentUser.setFirstName(nameParts[nameParts.length - 1]);
                    }
                    if (user.getLastName() == null || user.getLastName().isEmpty()) {
                        String lastName = String.join(" ", java.util.Arrays.copyOf(nameParts, nameParts.length - 1));
                        currentUser.setLastName(lastName);
                    }
                }
            }
            
            userService.update(currentUser);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/profile";
            
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/profile/edit";
        }
    }

    // Hiển thị form đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePassword() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "profile/change-password";
    }

    // Xử lý đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/profile/change-password";
        }

        // Kiểm tra mật khẩu mới và xác nhận khớp nhau
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/profile/change-password";
        }

        // Kiểm tra độ dài mật khẩu mới
        if (newPassword.length() < 8) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/profile/change-password";
        }

        try {
            // Cập nhật mật khẩu mới
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userService.update(currentUser);
            
            redirectAttributes.addAttribute("success", true);
            return "redirect:/profile/change-password";
            
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/profile/change-password";
        }
    }

    // Utility method để lấy user hiện tại
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            String email = authentication.getName();
            return userService.findByEmail(email).orElse(null);
        }
        return null;
    }
}