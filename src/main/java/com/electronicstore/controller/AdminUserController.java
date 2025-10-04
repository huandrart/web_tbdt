package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.service.UserService;
import com.electronicstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<User> users;
        
        // Apply filters
        if ((search != null && !search.trim().isEmpty()) || 
            (role != null && !role.isEmpty()) ||
            (status != null && !status.isEmpty())) {
            
            Boolean isActive = null;
            if (status != null && !status.isEmpty()) {
                isActive = Boolean.parseBoolean(status);
            }
            
            users = userService.searchUsers(search, role, isActive, pageable);
        } else {
            users = userService.findAll(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reversedDirection", direction.equals("asc") ? "desc" : "asc");
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        
        return "admin/users/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }
        
        // Get user's orders
        Page<com.electronicstore.entity.Order> orders = orderService.findByUserWithPagination(
            user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        model.addAttribute("user", user);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("totalOrders", orders.getTotalElements());
        
        return "admin/users/view";
    }
    
    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public String toggleUserStatus(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            
            if (user == null) {
                return "error";
            }
            
            user.setIsActive(!user.getIsActive());
            user.setUpdatedAt(LocalDateTime.now());
            userService.save(user);
            
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    @PostMapping("/update-role/{id}")
    public String updateUserRole(@PathVariable Long id,
                                @RequestParam String role,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
                return "redirect:/admin/users";
            }
            
            // Set role trực tiếp
            user.setRole(role.toUpperCase());
            user.setUpdatedAt(LocalDateTime.now());
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Quyền người dùng đã được cập nhật thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi cập nhật quyền: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
    }
    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
                return "redirect:/admin/users";
            }
            
            // Không cho phép xóa admin
            if ("ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản admin");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra xem user có đơn hàng hay không
            Page<com.electronicstore.entity.Order> orders = orderService.findByUserWithPagination(
                user, PageRequest.of(0, 1));
            
            if (orders.getTotalElements() > 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể xóa người dùng này vì còn có đơn hàng liên quan");
                return "redirect:/admin/users";
            }
            
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được xóa thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi xóa người dùng: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam("action") String action,
                            @RequestParam("userIds") List<Long> userIds,
                            RedirectAttributes redirectAttributes) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một người dùng");
                return "redirect:/admin/users";
            }
            
            switch (action) {
                case "activate":
                    for (Long userId : userIds) {
                        User user = userService.findById(userId);
                        if (user != null) {
                            user.setIsActive(true);
                            user.setUpdatedAt(LocalDateTime.now());
                            userService.save(user);
                        }
                    }
                    redirectAttributes.addFlashAttribute("success", 
                        "Đã kích hoạt " + userIds.size() + " người dùng thành công!");
                    break;
                    
                case "deactivate":
                    for (Long userId : userIds) {
                        User user = userService.findById(userId);
                        if (user != null && !"ADMIN".equals(user.getRole())) {
                            user.setIsActive(false);
                            user.setUpdatedAt(LocalDateTime.now());
                            userService.save(user);
                        }
                    }
                    redirectAttributes.addFlashAttribute("success", 
                        "Đã vô hiệu hóa " + userIds.size() + " người dùng thành công!");
                    break;
                    
                default:
                    redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    // Thay đổi vai trò người dùng
    @PostMapping("/{id}/change-role")
    public String changeUserRole(@PathVariable Long id, 
                                @RequestParam String newRole,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra role hợp lệ
            if (!newRole.equals("USER") && !newRole.equals("ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "Vai trò không hợp lệ");
                return "redirect:/admin/users";
            }
            
            // Cập nhật role
            String oldRole = user.getRole();
            user.setRole(newRole);
            user.setUpdatedAt(LocalDateTime.now());
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã thay đổi vai trò của " + user.getFullName() + 
                " từ " + oldRole + " thành " + newRole);
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi thay đổi vai trò: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}