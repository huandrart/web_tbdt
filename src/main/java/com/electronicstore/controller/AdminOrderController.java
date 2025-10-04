package com.electronicstore.controller;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.OrderItem;
import com.electronicstore.entity.User;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Order> orders;
        
        // Apply filters
        if ((status != null && !status.isEmpty()) ||
            (search != null && !search.trim().isEmpty()) ||
            (dateFrom != null && !dateFrom.isEmpty()) ||
            (dateTo != null && !dateTo.isEmpty())) {
            
            LocalDate fromDate = null;
            LocalDate toDate = null;
            
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDate.parse(dateFrom);
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDate.parse(dateTo);
            }
            
            orders = orderService.searchOrders(search, status, fromDate, toDate, pageable);
        } else {
            orders = orderService.findAll(pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reversedDirection", direction.equals("asc") ? "desc" : "asc");
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        
        return "admin/orders/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.findByIdDirect(id);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
            return "redirect:/admin/orders";
        }
        
        model.addAttribute("order", order);
        return "admin/orders/view";
    }
    
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   @RequestParam(required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findByIdDirect(id);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
                return "redirect:/admin/orders";
            }
            
            // Convert string to OrderStatus enum
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            if (notes != null && !notes.trim().isEmpty()) {
                order.setNotes(notes);
            }
            order.setUpdatedAt(LocalDateTime.now());
            
            orderService.save(order);
            
            redirectAttributes.addFlashAttribute("success", 
                "Trạng thái đơn hàng đã được cập nhật thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi cập nhật trạng thái: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/view/" + id;
    }
    
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findByIdDirect(id);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
                return "redirect:/admin/orders";
            }
            
            // Chỉ cho phép xóa đơn hàng ở trạng thái PENDING hoặc CANCELLED
            if (Order.OrderStatus.DELIVERED.equals(order.getStatus()) || 
                Order.OrderStatus.SHIPPING.equals(order.getStatus())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể xóa đơn hàng đã hoàn thành hoặc đang vận chuyển");
                return "redirect:/admin/orders";
            }
            
            orderService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xóa thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi xóa đơn hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
    
    @GetMapping("/print/{id}")
    public String printOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.findByIdDirect(id);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
            return "redirect:/admin/orders";
        }
        
        model.addAttribute("order", order);
        return "admin/orders/print";
    }
    
    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam("action") String action,
                            @RequestParam("orderIds") List<Long> orderIds,
                            RedirectAttributes redirectAttributes) {
        try {
            if (orderIds == null || orderIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một đơn hàng");
                return "redirect:/admin/orders";
            }
            
            switch (action) {
                case "delete":
                    for (Long orderId : orderIds) {
                        Order order = orderService.findByIdDirect(orderId);
                        if (order != null && 
                            !Order.OrderStatus.DELIVERED.equals(order.getStatus()) && 
                            !Order.OrderStatus.SHIPPING.equals(order.getStatus())) {
                            orderService.deleteById(orderId);
                        }
                    }
                    redirectAttributes.addFlashAttribute("success", 
                        "Đã xóa " + orderIds.size() + " đơn hàng thành công!");
                    break;
                    
                case "confirm":
                    for (Long orderId : orderIds) {
                        Order order = orderService.findByIdDirect(orderId);
                        if (order != null && Order.OrderStatus.PENDING.equals(order.getStatus())) {
                            order.setStatus(Order.OrderStatus.CONFIRMED);
                            order.setUpdatedAt(LocalDateTime.now());
                            orderService.save(order);
                        }
                    }
                    redirectAttributes.addFlashAttribute("success", 
                        "Đã xác nhận " + orderIds.size() + " đơn hàng thành công!");
                    break;
                    
                default:
                    redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
}