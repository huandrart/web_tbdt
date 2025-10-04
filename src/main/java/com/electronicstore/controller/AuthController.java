package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Trang login (GET) -> trả về template auth/login
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // Trang register (GET) -> trả về template auth/register
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            // Dùng trực tiếp entity User cho đơn giản (khớp th:object="${user}")
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    // Submit register (POST)
    @PostMapping("/register")
    public String doRegister(@ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             RedirectAttributes ra,
                             Model model) {
        // Validate tối thiểu (nếu entity chưa có annotation)
        if (isBlank(user.getFullName())) {
            bindingResult.rejectValue("fullName", "NotBlank", "Họ tên không được để trống");
        }
        if (isBlank(user.getEmail()) || !isEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "Email", "Email không hợp lệ");
        }
        if (isBlank(user.getPassword()) || user.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "Size", "Mật khẩu tối thiểu 6 ký tự");
        }

        if (bindingResult.hasErrors()) {
            // Trả lại form với lỗi
            model.addAttribute("user", user);
            return "auth/register";
        }

        try {
            // Kiểm tra trùng email
            if (userService.existsByEmail(user.getEmail())) {
                bindingResult.rejectValue("email", "Duplicate", "Email đã được sử dụng");
                model.addAttribute("user", user);
                return "auth/register";
            }

            // Gọi service: sẽ encode mật khẩu + set role USER + isActive = true
            userService.register(user);

            // Đăng ký thành công -> chuyển về trang login
            ra.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập.");
            return "redirect:/login";

        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registerError", ex.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        } catch (Exception ex) {
            bindingResult.reject("registerError", "Có lỗi xảy ra. Vui lòng thử lại.");
            model.addAttribute("user", user);
            return "auth/register";
        }
    }

    // Helpers
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private boolean isEmail(String s) {
        if (isBlank(s)) return false;
        return s.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
