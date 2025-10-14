package com.electronicstore.controller;

import com.electronicstore.dto.CartItem;
import com.electronicstore.entity.Cart;
import com.electronicstore.entity.Order;
import com.electronicstore.entity.OrderItem;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.UserService;
import com.electronicstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public String showCheckout(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Get user from authentication
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Get cart from database
        List<Cart> cartItems = cartService.findByUser(user);
        List<CartItem> cartItemDTOs = convertToCartItemDTOs(cartItems);
        
        if (cartItemDTOs.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Calculate totals
        BigDecimal subtotal = cartItemDTOs.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shippingFee = BigDecimal.valueOf(30000); // 30,000 VND
        BigDecimal total = subtotal.add(shippingFee);
        
        model.addAttribute("user", user);
        
        model.addAttribute("cartItems", cartItemDTOs);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        model.addAttribute("paymentMethods", Order.PaymentMethod.values());
        
        return "checkout/checkout";
    }
    
    @PostMapping("/process")
    public String processCheckout(@RequestParam("customerName") String customerName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam("paymentMethod") Order.PaymentMethod paymentMethod,
                                 @RequestParam(value = "notes", required = false) String notes,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để đặt hàng!");
            return "redirect:/login";
        }
        
        // Get user from authentication
        String userEmail = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin người dùng!");
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Get cart from database
        List<Cart> cartItems = cartService.findByUser(user);
        List<CartItem> cartItemDTOs = convertToCartItemDTOs(cartItems);
        
        if (cartItemDTOs.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống!");
            return "redirect:/cart";
        }
        
        try {
            // Convert CartItems to OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cartItemDTOs) {
                Product product = productService.findById(cartItem.getProductId());
                if (product != null) {
                    
                    // Check stock availability
                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Sản phẩm " + product.getName() + " không đủ hàng!");
                        return "redirect:/checkout";
                    }
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getUnitPrice());
                    orderItem.setTotalPrice(cartItem.getTotalPrice());
                    orderItems.add(orderItem);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Sản phẩm không tồn tại!");
                    return "redirect:/checkout";
                }
            }
            
            // Create order
            Order order = orderService.createOrder(user, orderItems, address, phone, customerName, notes);
            order.setPaymentMethod(paymentMethod);
            orderService.save(order);
            
            // Clear cart from database
            cartService.clearCart(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber());
            
            return "redirect:/checkout/success/" + order.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
    
    // Convert Cart entities to CartItem DTOs
    private List<CartItem> convertToCartItemDTOs(List<Cart> cartItems) {
        List<CartItem> cartItemDTOs = new ArrayList<>();
        
        for (Cart cart : cartItems) {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(cart.getProduct().getId());
            cartItem.setProductName(cart.getProduct().getName());
            cartItem.setUnitPrice(cart.getProduct().getCurrentPrice());
            cartItem.setQuantity(cart.getQuantity());
            cartItem.setTotalPrice(cart.getProduct().getCurrentPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            cartItem.setImageUrl(cart.getProduct().getImageUrls() != null && !cart.getProduct().getImageUrls().isEmpty() 
                ? cart.getProduct().getImageUrls().get(0) : "no-image.png");
            
            cartItemDTOs.add(cartItem);
        }
        
        return cartItemDTOs;
    }
    
    @GetMapping("/success/{orderId}")
    public String checkoutSuccess(@PathVariable Long orderId, Model model) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            return "checkout/success";
        } else {
            return "redirect:/";
        }
    }
}