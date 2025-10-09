package com.electronicstore.controller;

import com.electronicstore.entity.User;
import com.electronicstore.entity.UserRole;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/test")
    public String testUsers(Model model) {
        Page<User> users = userService.findAll(PageRequest.of(0, 10));
        model.addAttribute("users", users);
        return "admin/users/test";
    }
    
    @GetMapping("/view-simple/{id}")
    public String viewUserSimple(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }
        
        // Get user's orders
        Page<com.electronicstore.entity.Order> orders = orderService.findByUserWithPagination(
            user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        model.addAttribute("user", user);
        model.addAttribute("totalOrders", orders.getTotalElements());
        
        return "admin/users/view-simple";
    }
    
    @GetMapping("/view-fixed/{id}")
    public String viewUserFixed(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }
        
        // Get user's orders
        Page<com.electronicstore.entity.Order> orders = orderService.findByUserWithPagination(
            user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        model.addAttribute("user", user);
        model.addAttribute("totalOrders", orders.getTotalElements());
        
        return "admin/users/view-fixed";
    }
    
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
        
        try {
            System.out.println("AdminUserController.listUsers() called with page=" + page + ", size=" + size);
            
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
            
            System.out.println("Found " + users.getTotalElements() + " users, content size: " + users.getContent().size());
            
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
        } catch (Exception e) {
            System.err.println("Error in listUsers: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/users/list";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes,
                          Authentication authentication) {
        User user = userService.findById(id);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users";
        }
        
        // Get current user info
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUserEmail = authentication.getName();
            currentUser = userService.findByEmail(currentUserEmail).orElse(null);
        }
        
        // Get user's orders
        Page<com.electronicstore.entity.Order> orders = orderService.findByUserWithPagination(
            user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
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
    
    @GetMapping("/update-status/{id}")
    public String updateUserStatus(@PathVariable Long id, 
                                 @RequestParam String active,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + id);
                return "redirect:/admin/users";
            }
            
            // Convert string to boolean
            boolean isActive = Boolean.parseBoolean(active);
            
            // Kiểm tra quyền: không cho phép khóa SUPER_ADMIN
            if (user.getRole() == UserRole.SUPER_ADMIN && !isActive) {
                redirectAttributes.addFlashAttribute("error", "Không thể vô hiệu hóa tài khoản siêu quản trị");
                return "redirect:/admin/users/view/" + id;
            }
            
            user.setIsActive(isActive);
            user.setUpdatedAt(LocalDateTime.now());
            userService.save(user);
            
            String action = isActive ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("success", 
                "Đã " + action + " tài khoản " + user.getFullName() + " thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi cập nhật trạng thái: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
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
            user.setRole(UserRole.fromCode(role.toUpperCase()));
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
                                @RequestParam UserRole newRole,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra role hợp lệ
            if (newRole != UserRole.USER && newRole != UserRole.ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Vai trò không hợp lệ");
                return "redirect:/admin/users";
            }
            
            // Cập nhật role
            UserRole oldRole = user.getRole();
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
    
    // Đặt lại mật khẩu - GET redirect
    @GetMapping("/reset-password/{id}")
    public String resetPasswordForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return "redirect:/admin/users/view/" + id;
    }
    
    // Đặt lại mật khẩu - POST
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, 
                               @RequestParam String newPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra quyền: chỉ SUPER_ADMIN mới có thể reset password cho ADMIN
            if (user.getRole() == UserRole.ADMIN) {
                // TODO: Kiểm tra current user có phải SUPER_ADMIN không
                // if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
                //     redirectAttributes.addFlashAttribute("error", "Không có quyền reset password cho admin");
                //     return "redirect:/admin/users";
                // }
            }
            
            user.setPassword(newPassword);
            user.setUpdatedAt(LocalDateTime.now());
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Mật khẩu đã được đặt lại thành công cho " + user.getFullName());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi đặt lại mật khẩu: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
    }
    
    // Khóa/Mở khóa tài khoản - GET redirect
    @GetMapping("/toggle-lock/{id}")
    public String toggleLockForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return "redirect:/admin/users/view/" + id;
    }
    
    // Khóa/Mở khóa tài khoản - POST
    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable Long id, 
                            @RequestParam boolean locked,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra quyền: không cho phép khóa SUPER_ADMIN
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Không thể khóa tài khoản siêu quản trị");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra quyền: chỉ SUPER_ADMIN mới có thể khóa ADMIN
            if (user.getRole() == UserRole.ADMIN && locked) {
                // TODO: Kiểm tra current user có phải SUPER_ADMIN không
            }
            
            user.setIsActive(!locked);
            user.setUpdatedAt(LocalDateTime.now());
            userService.update(user);
            
            String action = locked ? "khóa" : "mở khóa";
            redirectAttributes.addFlashAttribute("success", 
                "Đã " + action + " tài khoản " + user.getFullName() + " thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
    }
    
    // Cấp quyền admin - GET redirect
    @GetMapping("/grant-admin/{id}")
    public String grantAdminForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return "redirect:/admin/users/view/" + id;
    }
    
    // Cấp quyền admin (chỉ SUPER_ADMIN mới có thể cấp) - POST
    @PostMapping("/grant-admin/{id}")
    public String grantAdmin(@PathVariable Long id, 
                            @RequestParam UserRole newRole,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra quyền: chỉ SUPER_ADMIN mới có thể cấp quyền admin
            // TODO: Kiểm tra current user có phải SUPER_ADMIN không
            
            // Không cho phép cấp SUPER_ADMIN qua web interface
            if (newRole == UserRole.SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Không thể cấp quyền siêu quản trị qua giao diện web");
                return "redirect:/admin/users";
            }
            
            UserRole oldRole = user.getRole();
            user.setRole(newRole);
            user.setUpdatedAt(LocalDateTime.now());
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã cấp quyền " + newRole.getDisplayName() + " cho " + user.getFullName() + 
                " (từ " + oldRole.getDisplayName() + ")");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi cấp quyền: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
    }
    
    // Thu hồi quyền admin - GET redirect
    @GetMapping("/revoke-admin/{id}")
    public String revokeAdminForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return "redirect:/admin/users/view/" + id;
    }
    
    // Thu hồi quyền admin (chỉ SUPER_ADMIN mới có thể thu hồi) - POST
    @PostMapping("/revoke-admin/{id}")
    public String revokeAdmin(@PathVariable Long id, 
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra quyền: chỉ SUPER_ADMIN mới có thể thu hồi quyền admin
            // TODO: Kiểm tra current user có phải SUPER_ADMIN không
            
            // Không cho phép thu hồi quyền SUPER_ADMIN
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Không thể thu hồi quyền siêu quản trị");
                return "redirect:/admin/users";
            }
            
            UserRole oldRole = user.getRole();
            user.setRole(UserRole.USER);
            user.setUpdatedAt(LocalDateTime.now());
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã thu hồi quyền " + oldRole.getDisplayName() + " của " + user.getFullName() + 
                " và chuyển về " + UserRole.USER.getDisplayName());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi thu hồi quyền: " + e.getMessage());
        }
        
        return "redirect:/admin/users/view/" + id;
    }
}