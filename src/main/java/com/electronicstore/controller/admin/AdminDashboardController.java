package com.electronicstore.controller.admin;

import com.electronicstore.service.ProductService;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            // Get statistics
            long totalProducts = productService.findAll().size();
            long totalUsers = userService.findAll().size();
            long totalOrders = orderService.findAll().size();
            
            // Add statistics to model
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalOrders", totalOrders);
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/dashboard";
        }
    }
}