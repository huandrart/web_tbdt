package com.electronicstore.controller;

import com.electronicstore.entity.Order;
import com.electronicstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/momo")
    public String momoPayment(@RequestParam Long orderId, 
                             @RequestParam BigDecimal totalAmount,
                             Model model) {
        
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("totalAmount", totalAmount);
        
        return "payment/momo";
    }
    
    @PostMapping("/momo/process")
    public String processMomoPayment(@RequestParam Long orderId,
                                   @RequestParam String momoResult,
                                   RedirectAttributes redirectAttributes) {
        
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Giả lập kết quả thanh toán MoMo
        if ("success".equals(momoResult)) {
            // Thanh toán thành công
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderService.save(order);
            
            redirectAttributes.addFlashAttribute("success", 
                "Thanh toán MoMo thành công! Mã đơn hàng: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId();
        } else {
            // Thanh toán thất bại
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            orderService.save(order);
            
            redirectAttributes.addFlashAttribute("error", 
                "Thanh toán MoMo thất bại! Đơn hàng đã bị hủy.");
            return "redirect:/orders";
        }
    }
}
