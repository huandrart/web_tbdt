-- ============================================================
-- DATABASE SETUP FOR ELECTRONIC STORE WEBSITE
-- Created: September 25, 2025
-- Description: Complete database schema with sample data
-- ============================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS electronic_store_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE electronic_store_db;

-- ============================================================
-- DROP TABLES (in reverse order to avoid foreign key issues)
-- ============================================================
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ============================================================
-- CREATE TABLES
-- ============================================================

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    date_of_birth DATE,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories Table  
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Products Table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    sku VARCHAR(100) UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    specifications JSON,
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Orders Table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED') DEFAULT 'PENDING',
    shipping_address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Order Items Table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reviews Table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product_review (user_id, product_id)
);

-- ============================================================
-- CREATE INDEXES FOR BETTER PERFORMANCE
-- ============================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);

-- Categories indexes
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(is_active);
CREATE INDEX idx_categories_display_order ON categories(display_order);

-- Products indexes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_featured ON products(is_featured);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_stock ON products(stock_quantity);

-- Orders indexes
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Order items indexes
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Reviews indexes
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- ============================================================
-- INSERT SAMPLE DATA
-- ============================================================

-- Insert Admin User
INSERT INTO users (first_name, last_name, email, password, role, phone_number, address, is_active) VALUES
('Admin', 'System', 'admin@electronicstore.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', 'ADMIN', '0123456789', '123 Admin Street, Ho Chi Minh City', TRUE);
-- Password: admin123

-- Insert Sample Users
INSERT INTO users (first_name, last_name, email, password, phone_number, address, date_of_birth, is_active) VALUES
('Nguyễn', 'Văn A', 'nguyenvana@gmail.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', '0987654321', '456 Nguyen Van Cu, District 1, Ho Chi Minh City', '1990-01-15', TRUE),
('Trần', 'Thị B', 'tranthib@gmail.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', '0901234567', '789 Le Loi, District 3, Ho Chi Minh City', '1985-05-20', TRUE),
('Lê', 'Văn C', 'levanc@gmail.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', '0912345678', '321 Tran Hung Dao, District 5, Ho Chi Minh City', '1992-08-10', TRUE),
('Phạm', 'Thị D', 'phamthid@gmail.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', '0923456789', '654 Vo Van Tan, District 7, Ho Chi Minh City', '1988-12-25', TRUE),
('Hoàng', 'Văn E', 'hoangvane@gmail.com', '$2a$10$YQHQz.8TvYJjNwNhNgXP5uF1g5lQg4ZyOBbH8qJgKj7rTqgHc4e9a', '0934567890', '987 Pasteur, District 1, Ho Chi Minh City', '1995-03-18', TRUE);
-- All passwords: password123

-- Insert Root Categories
INSERT INTO categories (name, description, display_order, is_active, image_url) VALUES
('Điện thoại & Tablet', 'Điện thoại thông minh, máy tính bảng và phụ kiện', 1, TRUE, 'phones-tablets.jpg'),
('Laptop & Máy tính', 'Laptop, máy tính để bàn, linh kiện máy tính', 2, TRUE, 'laptops-computers.jpg'),
('TV & Âm thanh', 'Tivi, loa, tai nghe, hệ thống âm thanh', 3, TRUE, 'tv-audio.jpg'),
('Gia dụng điện tử', 'Máy giặt, tủ lạnh, điều hòa, đồ gia dụng', 4, TRUE, 'appliances.jpg'),
('Gaming & Console', 'Console game, phụ kiện gaming', 5, TRUE, 'gaming.jpg'),
('Phụ kiện điện tử', 'Cáp sạc, pin dự phòng, ốp lưng, miếng dán', 6, TRUE, 'accessories.jpg');

-- Insert Sub-categories
INSERT INTO categories (name, description, parent_id, display_order, is_active, image_url) VALUES
-- Phone & Tablet subcategories
('iPhone', 'Điện thoại iPhone các dòng', 1, 1, TRUE, 'iphone.jpg'),
('Samsung Galaxy', 'Điện thoại Samsung Galaxy', 1, 2, TRUE, 'samsung.jpg'),
('Xiaomi', 'Điện thoại Xiaomi các dòng', 1, 3, TRUE, 'xiaomi.jpg'),
('iPad', 'Máy tính bảng iPad', 1, 4, TRUE, 'ipad.jpg'),
('Android Tablet', 'Máy tính bảng Android', 1, 5, TRUE, 'android-tablet.jpg'),

-- Laptop & Computer subcategories  
('MacBook', 'Laptop MacBook của Apple', 2, 1, TRUE, 'macbook.jpg'),
('Windows Laptop', 'Laptop chạy Windows', 2, 2, TRUE, 'windows-laptop.jpg'),
('Gaming Laptop', 'Laptop chuyên game', 2, 3, TRUE, 'gaming-laptop.jpg'),
('Máy tính để bàn', 'Desktop PC', 2, 4, TRUE, 'desktop.jpg'),
('Linh kiện máy tính', 'CPU, RAM, VGA, Mainboard', 2, 5, TRUE, 'components.jpg'),

-- TV & Audio subcategories
('Smart TV', 'Tivi thông minh', 3, 1, TRUE, 'smart-tv.jpg'),
('Loa Bluetooth', 'Loa không dây Bluetooth', 3, 2, TRUE, 'bluetooth-speaker.jpg'),
('Tai nghe', 'Tai nghe có dây và không dây', 3, 3, TRUE, 'headphones.jpg'),
('Soundbar', 'Loa soundbar cho TV', 3, 4, TRUE, 'soundbar.jpg');

-- Insert Sample Products
INSERT INTO products (name, description, price, stock_quantity, sku, brand, model, category_id, is_active, is_featured, image_url) VALUES
-- iPhones
('iPhone 15 Pro Max 256GB', 'iPhone 15 Pro Max với chip A17 Pro, camera 48MP, màn hình 6.7 inch Super Retina XDR', 32990000.00, 50, 'IP15PM-256-NT', 'Apple', 'iPhone 15 Pro Max', 7, TRUE, TRUE, 'iphone-15-pro-max.jpg'),
('iPhone 15 128GB', 'iPhone 15 với chip A16 Bionic, camera kép 48MP, màn hình 6.1 inch', 24990000.00, 75, 'IP15-128-BL', 'Apple', 'iPhone 15', 7, TRUE, TRUE, 'iphone-15.jpg'),
('iPhone 14 Pro 128GB', 'iPhone 14 Pro với chip A16 Bionic, camera 48MP, màn hình 6.1 inch ProMotion', 27990000.00, 30, 'IP14P-128-PP', 'Apple', 'iPhone 14 Pro', 7, TRUE, FALSE, 'iphone-14-pro.jpg'),

-- Samsung Galaxy
('Samsung Galaxy S24 Ultra 256GB', 'Galaxy S24 Ultra với S Pen, camera 200MP, màn hình 6.8 inch Dynamic AMOLED', 30990000.00, 40, 'SGS24U-256-BK', 'Samsung', 'Galaxy S24 Ultra', 8, TRUE, TRUE, 'galaxy-s24-ultra.jpg'),
('Samsung Galaxy S23 128GB', 'Galaxy S23 với chip Snapdragon 8 Gen 2, camera 50MP', 18990000.00, 60, 'SGS23-128-WH', 'Samsung', 'Galaxy S23', 8, TRUE, FALSE, 'galaxy-s23.jpg'),

-- Xiaomi
('Xiaomi 14 Ultra 512GB', 'Xiaomi 14 Ultra với camera Leica, chip Snapdragon 8 Gen 3', 25990000.00, 25, 'XM14U-512-BK', 'Xiaomi', '14 Ultra', 9, TRUE, TRUE, 'xiaomi-14-ultra.jpg'),
('Redmi Note 13 Pro 256GB', 'Redmi Note 13 Pro với camera 200MP, màn hình AMOLED 120Hz', 7990000.00, 100, 'RMN13P-256-BL', 'Xiaomi', 'Redmi Note 13 Pro', 9, TRUE, FALSE, 'redmi-note-13-pro.jpg'),

-- iPads
('iPad Pro 12.9 inch M2 256GB', 'iPad Pro với chip M2, màn hình Liquid Retina XDR 12.9 inch', 28990000.00, 20, 'IPADP129-M2-256', 'Apple', 'iPad Pro 12.9', 10, TRUE, TRUE, 'ipad-pro-129.jpg'),
('iPad Air 10.9 inch 128GB', 'iPad Air với chip M1, màn hình Liquid Retina 10.9 inch', 16990000.00, 35, 'IPADA109-M1-128', 'Apple', 'iPad Air', 10, TRUE, FALSE, 'ipad-air-109.jpg'),

-- MacBooks
('MacBook Pro 16 inch M3 Pro 512GB', 'MacBook Pro 16 inch với chip M3 Pro, RAM 18GB', 65990000.00, 15, 'MBP16-M3P-512', 'Apple', 'MacBook Pro 16', 12, TRUE, TRUE, 'macbook-pro-16-m3.jpg'),
('MacBook Air 13 inch M3 256GB', 'MacBook Air 13 inch với chip M3, siêu nhẹ chỉ 1.24kg', 32990000.00, 40, 'MBA13-M3-256', 'Apple', 'MacBook Air 13', 12, TRUE, TRUE, 'macbook-air-13-m3.jpg'),

-- Windows Laptops
('Dell XPS 13 Plus Core i7 512GB', 'Dell XPS 13 Plus với Core i7-1360P, RAM 16GB, SSD 512GB', 45990000.00, 25, 'DXPS13P-I7-512', 'Dell', 'XPS 13 Plus', 13, TRUE, FALSE, 'dell-xps-13-plus.jpg'),
('HP Spectre x360 Core i5 256GB', 'HP Spectre x360 2-in-1 với Core i5-1335U, màn hình cảm ứng', 35990000.00, 20, 'HPSX360-I5-256', 'HP', 'Spectre x360', 13, TRUE, FALSE, 'hp-spectre-x360.jpg'),

-- Gaming Laptops
('ASUS ROG Strix G16 RTX 4060', 'Gaming laptop ASUS ROG với Core i7, RTX 4060, RAM 16GB', 42990000.00, 18, 'ROGSG16-4060', 'ASUS', 'ROG Strix G16', 14, TRUE, TRUE, 'asus-rog-g16.jpg'),
('MSI Gaming GF63 Thin RTX 4050', 'MSI Gaming laptop với Core i5, RTX 4050, màn hình 15.6 inch 144Hz', 25990000.00, 30, 'MSIGF63-4050', 'MSI', 'GF63 Thin', 14, TRUE, FALSE, 'msi-gf63.jpg'),

-- Smart TVs
('Samsung Neo QLED 4K 65 inch', 'Smart TV Samsung Neo QLED 4K 65 inch với Quantum HDR', 35990000.00, 12, 'SNEOQ65-4K', 'Samsung', 'Neo QLED QN90C', 17, TRUE, TRUE, 'samsung-neo-qled-65.jpg'),
('LG OLED C3 55 inch 4K', 'Smart TV LG OLED C3 55 inch 4K với WebOS 23', 32990000.00, 15, 'LGOLED55-C3', 'LG', 'OLED C3', 17, TRUE, TRUE, 'lg-oled-c3-55.jpg'),

-- Bluetooth Speakers
('JBL Charge 5 Bluetooth Speaker', 'Loa Bluetooth JBL Charge 5 chống nước IP67, pin 20 giờ', 4990000.00, 50, 'JBLCH5-BL', 'JBL', 'Charge 5', 18, TRUE, FALSE, 'jbl-charge-5.jpg'),
('Sony SRS-XB43 Extra Bass', 'Loa Bluetooth Sony với Extra Bass, đèn LED Party', 5990000.00, 35, 'SONY-XB43', 'Sony', 'SRS-XB43', 18, TRUE, FALSE, 'sony-srs-xb43.jpg'),

-- Headphones
('Sony WH-1000XM5 Wireless', 'Tai nghe Sony WH-1000XM5 chống ồn hàng đầu', 8990000.00, 40, 'SONY-1000XM5', 'Sony', 'WH-1000XM5', 19, TRUE, TRUE, 'sony-wh1000xm5.jpg'),
('Apple AirPods Pro 2nd Gen', 'AirPods Pro thế hệ 2 với chip H2, chống ồn chủ động', 6490000.00, 60, 'APOD-PRO2', 'Apple', 'AirPods Pro 2', 19, TRUE, TRUE, 'airpods-pro-2.jpg'),
('Bose QuietComfort 45', 'Tai nghe Bose QC45 chống ồn, pin 24 giờ', 7990000.00, 25, 'BOSE-QC45', 'Bose', 'QuietComfort 45', 19, TRUE, FALSE, 'bose-qc45.jpg');

-- Insert Sample Orders
INSERT INTO orders (user_id, total_amount, status, shipping_address, notes) VALUES
(2, 32990000.00, 'DELIVERED', '456 Nguyen Van Cu, District 1, Ho Chi Minh City', 'Giao hàng trong giờ hành chính'),
(3, 49980000.00, 'SHIPPING', '789 Le Loi, District 3, Ho Chi Minh City', 'Gọi trước khi giao'),
(4, 25990000.00, 'PROCESSING', '321 Tran Hung Dao, District 5, Ho Chi Minh City', 'Thanh toán khi nhận hàng'),
(5, 65990000.00, 'CONFIRMED', '654 Vo Van Tan, District 7, Ho Chi Minh City', 'Kiểm tra kỹ sản phẩm'),
(6, 8990000.00, 'PENDING', '987 Pasteur, District 1, Ho Chi Minh City', 'Giao vào cuối tuần'),
(2, 16990000.00, 'DELIVERED', '456 Nguyen Van Cu, District 1, Ho Chi Minh City', 'Khách hàng thân thiết'),
(3, 42990000.00, 'CANCELLED', '789 Le Loi, District 3, Ho Chi Minh City', 'Khách hàng hủy do thay đổi ý định');

-- Insert Order Items
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
-- Order 1: iPhone 15 Pro Max
(1, 1, 1, 32990000.00),

-- Order 2: iPhone 15 + iPad Air
(2, 2, 1, 24990000.00),
(2, 9, 1, 24990000.00),

-- Order 3: Xiaomi 14 Ultra
(3, 6, 1, 25990000.00),

-- Order 4: MacBook Pro 16 inch
(4, 10, 1, 65990000.00),

-- Order 5: Sony Headphones
(5, 17, 1, 8990000.00),

-- Order 6: iPad Air
(6, 9, 1, 16990000.00),

-- Order 7: Gaming Laptop (Cancelled)
(7, 13, 1, 42990000.00);

-- Insert Sample Reviews
INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(2, 1, 5, 'iPhone 15 Pro Max rất tuyệt vời! Camera chụp đẹp, màn hình sắc nét, pin trâu.'),
(3, 2, 4, 'iPhone 15 tốt nhưng giá hơi cao. Tổng thể vẫn hài lòng với sản phẩm.'),
(4, 6, 5, 'Xiaomi 14 Ultra camera Leica quá xuất sắc! Chụp ảnh như máy ảnh chuyên nghiệp.'),
(5, 10, 5, 'MacBook Pro M3 Pro mạnh mẽ, màn hình đẹp, rất phù hợp cho công việc design.'),
(6, 17, 4, 'Sony WH-1000XM5 chống ồn rất tốt, âm thanh chi tiết. Đáng đồng tiền bát gạo.'),
(2, 9, 5, 'iPad Air M1 nhẹ, mỏng, hiệu năng mạnh. Dùng cho công việc và giải trí đều tốt.'),
(3, 13, 4, 'Gaming laptop ASUS ROG chạy game mượt, nhiệt độ ổn định. Thiết kế đẹp.'),
(4, 15, 5, 'Samsung Neo QLED chất lượng hình ảnh tuyệt vời, màu sắc sống động.'),
(5, 18, 4, 'JBL Charge 5 âm bass mạnh, chống nước tốt. Thích hợp cho outdoor.'),
(6, 19, 5, 'AirPods Pro 2 chống ồn hiệu quả, kết nối ổn định với iPhone.');

-- ============================================================
-- CREATE USEFUL VIEWS
-- ============================================================

-- View: Product sales statistics
CREATE VIEW product_sales_stats AS
SELECT 
    p.id,
    p.name,
    p.brand,
    p.price,
    COALESCE(SUM(oi.quantity), 0) as total_sold,
    COALESCE(SUM(oi.quantity * oi.price), 0) as total_revenue,
    COALESCE(AVG(r.rating), 0) as avg_rating,
    COUNT(r.id) as review_count
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status IN ('DELIVERED', 'SHIPPING')
LEFT JOIN reviews r ON p.id = r.product_id
GROUP BY p.id, p.name, p.brand, p.price;

-- View: Category sales statistics  
CREATE VIEW category_sales_stats AS
SELECT 
    c.id,
    c.name as category_name,
    COUNT(DISTINCT p.id) as product_count,
    COALESCE(SUM(oi.quantity), 0) as total_sold,
    COALESCE(SUM(oi.quantity * oi.price), 0) as total_revenue
FROM categories c
LEFT JOIN products p ON c.id = p.category_id AND p.is_active = TRUE
LEFT JOIN order_items oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status IN ('DELIVERED', 'SHIPPING')
GROUP BY c.id, c.name
ORDER BY total_revenue DESC;

-- View: User order statistics
CREATE VIEW user_order_stats AS
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    COUNT(o.id) as total_orders,
    COALESCE(SUM(o.total_amount), 0) as total_spent,
    COALESCE(AVG(o.total_amount), 0) as avg_order_value,
    MAX(o.created_at) as last_order_date
FROM users u
LEFT JOIN orders o ON u.id = o.user_id AND o.status IN ('DELIVERED', 'SHIPPING')
GROUP BY u.id, u.first_name, u.last_name, u.email;

-- ============================================================
-- CREATE STORED PROCEDURES
-- ============================================================

DELIMITER //

-- Procedure: Get dashboard statistics
CREATE PROCEDURE GetDashboardStats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM users WHERE role = 'USER') as total_users,
        (SELECT COUNT(*) FROM products WHERE is_active = TRUE) as total_products,
        (SELECT COUNT(*) FROM orders) as total_orders,
        (SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status IN ('DELIVERED', 'SHIPPING')) as total_revenue,
        (SELECT COUNT(*) FROM orders WHERE status = 'PENDING') as pending_orders,
        (SELECT COUNT(*) FROM orders WHERE status = 'SHIPPING') as shipping_orders,
        (SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED') as delivered_orders,
        (SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()) as today_orders;
END //

-- Procedure: Get low stock products
CREATE PROCEDURE GetLowStockProducts(IN threshold INT)
BEGIN
    SELECT 
        id,
        name,
        sku,
        stock_quantity,
        price,
        brand
    FROM products 
    WHERE stock_quantity <= threshold AND is_active = TRUE
    ORDER BY stock_quantity ASC;
END //

-- Procedure: Get top selling products
CREATE PROCEDURE GetTopSellingProducts(IN limit_count INT)
BEGIN
    SELECT 
        p.id,
        p.name,
        p.brand,
        p.price,
        SUM(oi.quantity) as total_sold,
        SUM(oi.quantity * oi.price) as total_revenue
    FROM products p
    JOIN order_items oi ON p.id = oi.product_id
    JOIN orders o ON oi.order_id = o.id
    WHERE o.status IN ('DELIVERED', 'SHIPPING')
    GROUP BY p.id, p.name, p.brand, p.price
    ORDER BY total_sold DESC
    LIMIT limit_count;
END //

DELIMITER ;

-- ============================================================
-- SAMPLE PROCEDURE CALLS (for testing)
-- ============================================================

-- Test dashboard stats
CALL GetDashboardStats();

-- Test low stock products (threshold = 20)
CALL GetLowStockProducts(20);

-- Test top selling products (top 5)
CALL GetTopSellingProducts(5);

-- ============================================================
-- USEFUL QUERIES FOR TESTING
-- ============================================================

-- Check all tables have data
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'categories', COUNT(*) FROM categories  
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL  
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL
SELECT 'reviews', COUNT(*) FROM reviews;

-- Show sample data from each table
SELECT 'Users Sample:' as info;
SELECT id, first_name, last_name, email, role, is_active FROM users LIMIT 3;

SELECT 'Categories Sample:' as info;
SELECT id, name, parent_id, is_active FROM categories LIMIT 5;

SELECT 'Products Sample:' as info;  
SELECT id, name, brand, price, stock_quantity, category_id FROM products LIMIT 5;

SELECT 'Orders Sample:' as info;
SELECT id, user_id, total_amount, status, created_at FROM orders LIMIT 3;

-- ============================================================
-- ADMIN ACCOUNT INFO
-- ============================================================
/*
ADMIN LOGIN CREDENTIALS:
- Email: admin@electronicstore.com  
- Password: admin123
- Role: ADMIN

TEST USER ACCOUNTS:
- All test users have password: password123
- Emails: nguyenvana@gmail.com, tranthib@gmail.com, etc.
*/

-- ============================================================
-- END OF SCRIPT
-- ============================================================

SELECT 'Database setup completed successfully!' as status;
SELECT 'Total tables created: 6' as info;
SELECT 'Total sample products: 19' as info;  
SELECT 'Total sample orders: 7' as info;
SELECT 'Admin account created with email: admin@electronicstore.com' as admin_info;