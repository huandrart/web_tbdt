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
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "notes", required = false) String notes,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        // Basic validation
        if (customerName == null || customerName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên không được để trống!");
            return "redirect:/checkout";
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không được để trống!");
            return "redirect:/checkout";
        }
        
        if (address == null || address.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ giao hàng không được để trống!");
            return "redirect:/checkout";
        }
        
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
            // Validate payment method
            Order.PaymentMethod paymentMethodEnum;
            try {
                paymentMethodEnum = Order.PaymentMethod.valueOf(paymentMethod);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ!");
                return "redirect:/checkout";
            }
            
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
            
            // Create order first
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(address);
            order.setPhone(phone);
            order.setCustomerName(customerName);
            order.setNotes(notes);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setPaymentMethod(paymentMethodEnum);
            
            // Calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItem item : orderItems) {
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
            order.setTotalAmount(totalAmount);
            
            // Save order first to get ID
            order = orderService.save(order);
            
            // Set order reference for each order item
            for (OrderItem item : orderItems) {
                item.setOrder(order);
            }
            order.setOrderItems(orderItems);
            
            // Save order again with items
            order = orderService.save(order);
            
            // Clear cart from database
            cartService.clearCart(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber());
            
            // Redirect based on payment method
            if (paymentMethodEnum == Order.PaymentMethod.E_WALLET) {
                return "redirect:/payment/momo?orderId=" + order.getId() + "&totalAmount=" + order.getTotalAmount();
            } else {
                return "redirect:/payment/success/" + order.getId() + "?paymentMethod=" + paymentMethodEnum.name();
            }
            
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