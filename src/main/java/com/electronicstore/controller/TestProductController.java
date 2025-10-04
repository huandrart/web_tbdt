package com.electronicstore.controller;

import com.electronicstore.repository.ProductRepository;
import com.electronicstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping("/test-products")
    public String testProducts(Model model) {
        try {
            long productCount = productRepository.count();
            long categoryCount = categoryRepository.count();
            
            model.addAttribute("productCount", productCount);
            model.addAttribute("categoryCount", categoryCount);
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());
            
            return "test-products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
            return "test-products";
        }
    }
    
    @GetMapping("/admin/test-products")
    public String testAdminProducts(Model model) {
        try {
            long productCount = productRepository.count();
            long categoryCount = categoryRepository.count();
            
            model.addAttribute("productCount", productCount);
            model.addAttribute("categoryCount", categoryCount);
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("message", "Admin test products page loaded successfully!");
            
            return "test-products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
            return "test-products";
        }
    }
}
