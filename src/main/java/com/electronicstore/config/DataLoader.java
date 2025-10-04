package com.electronicstore.config;

import com.electronicstore.entity.Category;
import com.electronicstore.entity.Product;
import com.electronicstore.entity.User;
import com.electronicstore.repository.CategoryRepository;
import com.electronicstore.repository.ProductRepository;
import com.electronicstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("DataLoader starting...");
        
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@electronicstore.com")) {
            User admin = new User();
            admin.setFullName("Administrator");
            admin.setEmail("admin@electronicstore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("0123456789");
            admin.setAddress("Admin Office");
            admin.setRole("ADMIN");
            admin.setIsActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("Created admin user: admin@electronicstore.com / admin123");
        }

        // Create sample user if not exists
        if (!userRepository.existsByEmail("user@example.com")) {
            User user = new User();
            user.setFullName("Nguyễn Văn A");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setPhone("0987654321");
            user.setAddress("123 Nguyễn Trãi, Q1, TP.HCM");
            user.setRole("USER");
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Created sample user: user@example.com / user123");
        }

        // Create sample categories if not exists
        if (categoryRepository.count() == 0) {
            createSampleCategories();
        }

        // Create sample products if not exists
        if (productRepository.count() == 0) {
            System.out.println("Creating sample products...");
            createSampleProducts();
        } else {
            System.out.println("Products already exist: " + productRepository.count());
        }
    }

    private void createSampleCategories() {
        Category laptop = new Category();
        laptop.setName("Laptop");
        laptop.setDescription("Máy tính xách tay các loại");
        laptop.setIsActive(true);
        laptop.setCreatedAt(LocalDateTime.now());
        laptop.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(laptop);

        Category smartphone = new Category();
        smartphone.setName("Smartphone");
        smartphone.setDescription("Điện thoại thông minh");
        smartphone.setIsActive(true);
        smartphone.setCreatedAt(LocalDateTime.now());
        smartphone.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(smartphone);

        Category tablet = new Category();
        tablet.setName("Tablet");
        tablet.setDescription("Máy tính bảng");
        tablet.setIsActive(true);
        tablet.setCreatedAt(LocalDateTime.now());
        tablet.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(tablet);

        Category accessories = new Category();
        accessories.setName("Phụ kiện");
        accessories.setDescription("Phụ kiện điện tử");
        accessories.setIsActive(true);
        accessories.setCreatedAt(LocalDateTime.now());
        accessories.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(accessories);

        System.out.println("Created sample categories");
    }

    private void createSampleProducts() {
        Category laptop = categoryRepository.findByName("Laptop").orElse(null);
        Category smartphone = categoryRepository.findByName("Smartphone").orElse(null);
        Category tablet = categoryRepository.findByName("Tablet").orElse(null);
        Category accessories = categoryRepository.findByName("Phụ kiện").orElse(null);

        if (laptop != null) {
            // Laptop products
            Product macbook = new Product();
            macbook.setName("MacBook Air M2");
            macbook.setDescription("Laptop Apple MacBook Air với chip M2, màn hình 13.6 inch");
            macbook.setSku("MBA-M2-001");
            macbook.setPrice(new BigDecimal("29990000"));
            macbook.setStockQuantity(15);
            macbook.setCategory(laptop);
            macbook.setIsActive(true);
            macbook.setCreatedAt(LocalDateTime.now());
            macbook.setUpdatedAt(LocalDateTime.now());
            productRepository.save(macbook);

            Product dell = new Product();
            dell.setName("Dell XPS 13");
            dell.setDescription("Laptop Dell XPS 13 với Intel Core i7");
            dell.setSku("DELL-XPS13-001");
            dell.setPrice(new BigDecimal("25990000"));
            dell.setStockQuantity(3); // Low stock
            dell.setCategory(laptop);
            dell.setIsActive(true);
            dell.setCreatedAt(LocalDateTime.now());
            dell.setUpdatedAt(LocalDateTime.now());
            productRepository.save(dell);
        }

        if (smartphone != null) {
            // Smartphone products
            Product iphone = new Product();
            iphone.setName("iPhone 15 Pro");
            iphone.setDescription("iPhone 15 Pro 128GB màu xanh titan");
            iphone.setSku("IP15-PRO-001");
            iphone.setPrice(new BigDecimal("28990000"));
            iphone.setStockQuantity(20);
            iphone.setCategory(smartphone);
            iphone.setIsActive(true);
            iphone.setCreatedAt(LocalDateTime.now());
            iphone.setUpdatedAt(LocalDateTime.now());
            productRepository.save(iphone);

            Product samsung = new Product();
            samsung.setName("Samsung Galaxy S24");
            samsung.setDescription("Samsung Galaxy S24 128GB màu đen");
            samsung.setSku("SGS24-001");
            samsung.setPrice(new BigDecimal("22990000"));
            samsung.setStockQuantity(2); // Low stock
            samsung.setCategory(smartphone);
            samsung.setIsActive(true);
            samsung.setCreatedAt(LocalDateTime.now());
            samsung.setUpdatedAt(LocalDateTime.now());
            productRepository.save(samsung);
        }

        if (tablet != null) {
            Product ipad = new Product();
            ipad.setName("iPad Air");
            ipad.setDescription("iPad Air 64GB Wi-Fi");
            ipad.setSku("IPAD-AIR-001");
            ipad.setPrice(new BigDecimal("15990000"));
            ipad.setStockQuantity(1); // Low stock
            ipad.setCategory(tablet);
            ipad.setIsActive(true);
            ipad.setCreatedAt(LocalDateTime.now());
            ipad.setUpdatedAt(LocalDateTime.now());
            productRepository.save(ipad);
        }

        if (accessories != null) {
            Product airpods = new Product();
            airpods.setName("AirPods Pro 2");
            airpods.setDescription("Tai nghe AirPods Pro thế hệ 2");
            airpods.setSku("APP-2-001");
            airpods.setPrice(new BigDecimal("6490000"));
            airpods.setStockQuantity(25);
            airpods.setCategory(accessories);
            airpods.setIsActive(true);
            airpods.setCreatedAt(LocalDateTime.now());
            airpods.setUpdatedAt(LocalDateTime.now());
            productRepository.save(airpods);
        }

        System.out.println("Created sample products");
    }
}