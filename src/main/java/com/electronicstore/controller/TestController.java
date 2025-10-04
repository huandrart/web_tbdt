package com.electronicstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Test successful!");
        return "test";
    }
    
    @GetMapping("/admin/test")
    public String adminTest(Model model) {
        model.addAttribute("message", "Admin test successful!");
        return "admin/test";
    }
}
