package com.electronicstore.controller;

import com.electronicstore.entity.Order;
import com.electronicstore.entity.ShipperReview;
import com.electronicstore.entity.User;
import com.electronicstore.service.OrderService;
import com.electronicstore.service.ShipperReviewService;
import com.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/shipper-reviews")
public class ShipperReviewController {

    @Autowired
    private ShipperReviewService shipperReviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/create/{orderId}")
    public String createReviewForm(@PathVariable Long orderId, Authentication authentication, Model model) {
        try {
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                model.addAttribute("error", "Không tìm thấy đơn hàng");
                return "error/404";
            }

            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser == null) {
                model.addAttribute("error", "Không tìm thấy người dùng");
                return "error/404";
            }
            
            // Check if user can review this order
            if (!shipperReviewService.canReviewOrder(order, currentUser)) {
                model.addAttribute("error", "Bạn không thể đánh giá đơn hàng này");
                return "error/403";
            }

            // Check if already reviewed
            Optional<ShipperReview> existingReview = shipperReviewService.findByOrder(order);
            if (existingReview.isPresent()) {
                model.addAttribute("review", existingReview.get());
                return "shipper-reviews/edit";
            }

            model.addAttribute("order", order);
            model.addAttribute("shipperReview", new ShipperReview());
            return "shipper-reviews/create";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/create")
    public String createReview(
            @RequestParam Long orderId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/orders/my-orders";
            }

            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/orders/my-orders";
            }
            
            // Check if user can review this order
            if (!shipperReviewService.canReviewOrder(order, currentUser)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không thể đánh giá đơn hàng này");
                return "redirect:/orders/my-orders";
            }

            // Check if already reviewed
            if (shipperReviewService.hasReviewedOrder(order)) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá đơn hàng này rồi");
                return "redirect:/orders/my-orders";
            }

            // Create new review
            ShipperReview review = new ShipperReview();
            review.setOrder(order);
            review.setShipper(order.getShipper());
            review.setCustomer(currentUser);
            review.setRating(rating);
            review.setComment(comment);

            shipperReviewService.save(review);

            redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá shipper!");
            return "redirect:/orders/my-orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo đánh giá: " + e.getMessage());
            return "redirect:/orders/my-orders";
        }
    }

    @GetMapping("/edit/{orderId}")
    public String editReviewForm(@PathVariable Long orderId, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                model.addAttribute("error", "Không tìm thấy đơn hàng");
                return "error/404";
            }

            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser == null) {
                model.addAttribute("error", "Không tìm thấy người dùng");
                return "error/404";
            }
            
            // Check if user can review this order
            if (!shipperReviewService.canReviewOrder(order, currentUser)) {
                model.addAttribute("error", "Bạn không thể đánh giá đơn hàng này");
                return "error/403";
            }

            Optional<ShipperReview> review = shipperReviewService.findByOrder(order);
            if (!review.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
                return "redirect:/orders/my-orders";
            }

            model.addAttribute("order", order);
            model.addAttribute("shipperReview", review.get());
            return "shipper-reviews/edit";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/edit")
    public String editReview(
            @RequestParam Long orderId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/orders/my-orders";
            }

            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/orders/my-orders";
            }
            
            // Check if user can review this order
            if (!shipperReviewService.canReviewOrder(order, currentUser)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không thể đánh giá đơn hàng này");
                return "redirect:/orders/my-orders";
            }

            Optional<ShipperReview> reviewOpt = shipperReviewService.findByOrder(order);
            if (!reviewOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
                return "redirect:/orders/my-orders";
            }

            ShipperReview review = reviewOpt.get();
            review.setRating(rating);
            review.setComment(comment);

            shipperReviewService.save(review);

            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được cập nhật!");
            return "redirect:/orders/my-orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật đánh giá: " + e.getMessage());
            return "redirect:/orders/my-orders";
        }
    }
}
