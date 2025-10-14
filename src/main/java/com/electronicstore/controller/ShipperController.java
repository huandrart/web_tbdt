package com.electronicstore.controller;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.User;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
        if (currentUser == null) {
            model.addAttribute("error", "Không tìm thấy người dùng");
            return "error/404";
        }
        model.addAttribute("user", currentUser);
        
        // Thống kê đơn hàng cho shipper (cá nhân hóa)
        long totalOrders = orderService.countByStatus(Order.OrderStatus.SHIPPING);
        long deliveredToday = orderService.countDeliveredTodayByShipper(currentUser);
        long pendingDelivery = orderService.countByStatus(Order.OrderStatus.SHIPPING);
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredToday", deliveredToday);
        model.addAttribute("pendingDelivery", pendingDelivery);
        
        return "shipper/dashboard";
    }

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "desc") String sortDir,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {
        try {
            System.out.println("=== ShipperController.listOrders START ===");
            System.out.println("Parameters: sortBy=" + sortBy + ", sortDir=" + sortDir + 
                             ", page=" + page + ", size=" + size + ", status=" + status);
            
            // Validate sort field
            String validSortBy = validateSortField(sortBy);
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, validSortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get orders for shipper (PENDING and SHIPPING status)
            Page<Order> orderPage;
            if (status != null && !status.isEmpty()) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderService.findByStatusWithUser(orderStatus, pageable);
                System.out.println("Filtered by status: " + orderStatus + ", found: " + orderPage.getTotalElements());
            } else {
                // Default: Get all orders for shipper (PENDING, PROCESSING, SHIPPING, DELIVERED)
                System.out.println("Getting all orders for shipper...");
                List<Order.OrderStatus> allowedStatuses = List.of(
                    Order.OrderStatus.PENDING,
                    Order.OrderStatus.PROCESSING, 
                    Order.OrderStatus.SHIPPING,
                    Order.OrderStatus.DELIVERED
                );
                orderPage = orderService.findByStatusIn(allowedStatuses, pageable);
                System.out.println("Orders found: " + orderPage.getTotalElements());
            }
            
            // Debug logging - theo mẫu AdminOrderController
            System.out.println("=== DEBUG: Order Statistics ===");
            System.out.println("Order page: " + (orderPage != null ? "NOT NULL" : "NULL"));
            System.out.println("Order content size: " + (orderPage != null && orderPage.getContent() != null ? orderPage.getContent().size() : "NULL"));
            System.out.println("Total elements: " + (orderPage != null ? orderPage.getTotalElements() : "NULL"));
            System.out.println("Orders in current page: " + (orderPage != null ? orderPage.getContent().size() : 0));
            
            if (orderPage != null && orderPage.getContent() != null) {
                System.out.println("=== ORDER DETAILS ===");
                for (int i = 0; i < orderPage.getContent().size(); i++) {
                    Order order = orderPage.getContent().get(i);
                    System.out.println("Order " + (i+1) + ": ID=" + order.getId() + 
                                     ", Number=" + order.getOrderNumber() + 
                                     ", User=" + (order.getUser() != null ? order.getUser().getFullName() : "NULL") +
                                     ", Total=" + order.getTotalAmount() +
                                     ", Status=" + order.getStatus() +
                                     ", Created=" + order.getCreatedAt());
                }
            }
            System.out.println("=== END DEBUG ===");
            
            model.addAttribute("orders", orderPage);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reversedDirection", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orderPage.getTotalPages());
            model.addAttribute("totalItems", orderPage.getTotalElements());
            model.addAttribute("status", status);
            
            System.out.println("Successfully prepared model for shipper/orders/list");
            System.out.println("=== ShipperController.listOrders END ===");
            
            return "shipper/orders/list";
            
        } catch (Exception e) {
            System.err.println("=== ERROR in ShipperController.listOrders ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END ERROR ===");
            
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
            return "shipper/orders/list";
        }
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== ShipperController.viewOrder START ===");
            System.out.println("Looking for order with ID: " + id);
            
            Order order = orderService.findByIdDirect(id);
            
            if (order == null) {
                System.out.println("Order not found, setting order = null in model");
                model.addAttribute("order", null);
                model.addAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
                return "shipper/orders/view";
            }
            
            System.out.println("Found order: " + order.getId() + " - " + order.getOrderNumber());
            System.out.println("Order status: " + order.getStatus());
            System.out.println("Order user: " + (order.getUser() != null ? order.getUser().getFullName() : "null"));
            System.out.println("Order items count: " + (order.getOrderItems() != null ? order.getOrderItems().size() : "null"));
            
            // Shipper can view all orders, but can only update certain statuses
            System.out.println("Order status: " + order.getStatus() + " - Shipper can view this order");
            
            model.addAttribute("order", order);
            System.out.println("Successfully prepared model for shipper/orders/view");
            System.out.println("=== ShipperController.viewOrder END ===");
            
            return "shipper/orders/view";
            
        } catch (Exception e) {
            System.err.println("=== ERROR in ShipperController.viewOrder ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END ERROR ===");
            
            model.addAttribute("order", null);
            model.addAttribute("error", "Có lỗi xảy ra khi tải đơn hàng: " + e.getMessage());
            return "shipper/orders/view";
        }
    }

    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus newStatus,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            System.out.println("=== ShipperController.updateOrderStatus START ===");
            System.out.println("Order ID: " + id + ", New Status: " + newStatus);
            
            Order order = orderService.findByIdDirect(id);
            if (order == null) {
                System.out.println("Order not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
                return "redirect:/shipper/orders";
            }
            
            System.out.println("Found order: " + order.getId() + " - " + order.getOrderNumber());
            System.out.println("Current status: " + order.getStatus());
            
            // Get current shipper
            User currentShipper = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentShipper == null) {
                System.out.println("Shipper not found: " + authentication.getName());
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy shipper");
                return "redirect:/shipper/orders";
            }
            
            System.out.println("Current shipper: " + currentShipper.getFullName());
            
            // Validate status transition for shipper
            if (!isValidShipperStatusTransition(order.getStatus(), newStatus)) {
                System.out.println("Invalid status transition: " + order.getStatus() + " -> " + newStatus);
                redirectAttributes.addFlashAttribute("error", "Không thể chuyển từ trạng thái " + 
                    order.getStatus().getDisplayName() + " sang " + newStatus.getDisplayName());
                return "redirect:/shipper/orders/" + id;
            }
            
            // Update order status and assign shipper
            Order.OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            order.setShipper(currentShipper);
            if (notes != null && !notes.trim().isEmpty()) {
                order.setNotes(notes);
            }
            
            orderService.save(order);
            
            System.out.println("Order status updated successfully: " + oldStatus + " -> " + newStatus);
            System.out.println("=== ShipperController.updateOrderStatus END ===");
            
            String message = "Cập nhật trạng thái đơn hàng thành công từ '" + oldStatus.getDisplayName() + 
                           "' sang '" + newStatus.getDisplayName() + "'";
            redirectAttributes.addFlashAttribute("success", message);
            
            return "redirect:/shipper/orders/" + id;
            
        } catch (Exception e) {
            System.err.println("=== ERROR in ShipperController.updateOrderStatus ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END ERROR ===");
            
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/shipper/orders/" + id;
        }
    }

    /**
     * Validate status transition for shipper
     * Shipper can change from PENDING to PROCESSING, and from SHIPPING to DELIVERED or CANCELLED
     */
    private boolean isValidShipperStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        if (currentStatus == Order.OrderStatus.PENDING) {
            return newStatus == Order.OrderStatus.PROCESSING || newStatus == Order.OrderStatus.CANCELLED;
        }
        if (currentStatus == Order.OrderStatus.SHIPPING) {
            return newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.CANCELLED;
        }
        return false;
    }

    /**
     * Validate sort field for orders
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt";
        }
        
        String[] validSortFields = {
            "id", "orderNumber", "customerName", "totalAmount", "status", 
            "createdAt", "updatedAt", "paymentMethod"
        };
        
        for (String validField : validSortFields) {
            if (validField.equals(sortBy)) {
                return sortBy;
            }
        }
        
        return "createdAt";
    }
}
