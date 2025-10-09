package com.electronicstore.controller;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.User;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class UserOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    // Xem danh sách đơn hàng của user
    @GetMapping({"/", "/my-orders"})
    public String myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;
        
        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.findByUserAndStatus(currentUser, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                orders = orderService.findByUserWithPagination(currentUser, pageable);
            }
        } else {
            orders = orderService.findByUserWithPagination(currentUser, pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("status", status);
        model.addAttribute("statusOptions", Order.OrderStatus.values());
        
        return "orders/my-orders";
    }
    
    // Xem chi tiết đơn hàng
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user hiện tại không
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        return "orders/order-detail";
    }
    
    // Hủy đơn hàng (chỉ cho phép với đơn hàng PENDING)
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Kiểm tra xem đơn hàng có thuộc về user hiện tại không
        if (!order.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
            return "redirect:/orders";
        }
        
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi hủy đơn hàng!");
        }
        
        return "redirect:/orders";
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
