package com.electronicstore.service;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.OrderItem;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductService productService;
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    public Page<Order> findAllWithPagination(Pageable pageable) {
        return orderRepository.findAllOrdersByCreatedAtDesc(pageable);
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Order findByIdDirect(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.orElse(null);
    }
    
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }
    
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }
    
    public Page<Order> findByUserWithPagination(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> findPendingOrders() {
        return orderRepository.findPendingOrders();
    }
    
    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate);
    }
    
    public Order save(Order order) {
        return orderRepository.save(order);
    }
    
    public Order createOrder(User user, List<OrderItem> orderItems, String shippingAddress, 
                           String phone, String customerName, String notes) {
        // Validate stock availability before creating order
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại");
            }
            
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (currentStock < item.getQuantity()) {
                throw new IllegalArgumentException("Không đủ hàng trong kho cho sản phẩm: " + product.getName() + 
                    ". Tồn kho: " + currentStock + ", Yêu cầu: " + item.getQuantity());
            }
        }
        
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setCustomerName(customerName);
        order.setNotes(notes);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        
        // Save order first
        Order savedOrder = orderRepository.save(order);
        
        // Update stock for each product after order is saved
        try {
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock - item.getQuantity());
                productService.save(product);
            }
        } catch (Exception e) {
            // If stock update fails, delete the order and restore stock
            orderRepository.delete(savedOrder);
            throw new IllegalArgumentException("Không thể cập nhật tồn kho: " + e.getMessage());
        }
        
        return savedOrder;
    }
    
    public Order updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Đơn hàng không tồn tại");
    }
    
    public Order updatePaymentStatus(Long orderId, Order.PaymentStatus newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(newStatus);
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Đơn hàng không tồn tại");
    }
    
    public void cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            if (order.getStatus() == Order.OrderStatus.PENDING || 
                order.getStatus() == Order.OrderStatus.CONFIRMED) {
                
                // Restore stock for each order item
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    if (product != null) {
                        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                        product.setStockQuantity(currentStock + item.getQuantity());
                        productService.save(product);
                    }
                }
                
                order.setStatus(Order.OrderStatus.CANCELLED);
                order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                orderRepository.save(order);
            } else {
                throw new IllegalArgumentException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus().getDisplayName());
            }
        } else {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }
    }
    
    public void restoreStockForOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            // Restore stock for each order item
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    product.setStockQuantity(currentStock + item.getQuantity());
                    productService.save(product);
                }
            }
        }
    }
    
    public void updateStockForOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            // Update stock for each order item
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    int newStock = currentStock - item.getQuantity();
                    
                    if (newStock < 0) {
                        throw new IllegalArgumentException("Không đủ hàng trong kho cho sản phẩm: " + product.getName());
                    }
                    
                    product.setStockQuantity(newStock);
                    productService.save(product);
                }
            }
        }
    }
    
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getTotalRevenueByDateRange(startDate, endDate);
    }
    
    public long countOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.countOrdersByDateRange(startDate, endDate);
    }
    
    public long countOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }
    
    public long countAllOrders() {
        return orderRepository.count();
    }
    
    public Page<Order> searchOrders(String search, String status, 
                                   java.time.LocalDate fromDate, 
                                   java.time.LocalDate toDate, 
                                   Pageable pageable) {
        // For now, return all orders - search functionality can be enhanced later
        // This is a placeholder implementation
        return orderRepository.findAll(pageable);
    }
    
    public long count() {
        return orderRepository.count();
    }
    
    public long countByStatus(Order.OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }
    
    public long countByUser(User user) {
        return orderRepository.countByUser(user);
    }
    
    public Page<Order> findByUserAndStatus(User user, Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserAndStatus(user, status, pageable);
    }
    
    public Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    public long countDeliveredToday() {
        return orderRepository.countDeliveredToday();
    }
    
    public long countDeliveredTodayByShipper(User shipper) {
        return orderRepository.countDeliveredTodayByShipper(shipper);
    }
    
    public Page<Order> findByStatusIn(List<Order.OrderStatus> statuses, Pageable pageable) {
        return orderRepository.findByStatusIn(statuses, pageable);
    }

    public Page<Order> findByStatusWithUser(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusWithUser(status, pageable);
    }

}