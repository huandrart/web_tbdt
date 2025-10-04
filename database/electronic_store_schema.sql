-- =============================================
-- Electronic Store Database Schema
-- Tạo cơ sở dữ liệu cho website bán thiết bị điện tử
-- =============================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS electronic_store_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE electronic_store_db;

-- Bảng danh mục sản phẩm
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categories_name (name),
    INDEX idx_categories_active (is_active)
);

-- Bảng người dùng
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    role ENUM('CUSTOMER', 'ADMIN', 'MANAGER') DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_active (is_active)
);

-- Bảng sản phẩm
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    sale_price DECIMAL(12,2),
    stock_quantity INT DEFAULT 0,
    sku VARCHAR(50) UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    warranty_period INT COMMENT 'Thời gian bảo hành (tháng)',
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    view_count BIGINT DEFAULT 0,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_products_category (category_id),
    INDEX idx_products_name (name),
    INDEX idx_products_brand (brand),
    INDEX idx_products_sku (sku),
    INDEX idx_products_price (price),
    INDEX idx_products_featured (is_featured),
    INDEX idx_products_active (is_active),
    INDEX idx_products_view_count (view_count),
    INDEX idx_products_created (created_at)
);

-- Bảng hình ảnh sản phẩm
CREATE TABLE product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_images_product (product_id)
);

-- Bảng đơn hàng
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    shipping_address TEXT NOT NULL,
    phone VARCHAR(15) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    notes TEXT,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED') DEFAULT 'PENDING',
    payment_method ENUM('CASH_ON_DELIVERY', 'BANK_TRANSFER', 'CREDIT_CARD', 'E_WALLET'),
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_number (order_number),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_created (created_at)
);

-- Bảng chi tiết đơn hàng
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id)
);

-- Bảng đánh giá sản phẩm
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user (user_id),
    INDEX idx_reviews_rating (rating),
    INDEX idx_reviews_approved (is_approved),
    UNIQUE KEY unique_user_product_review (user_id, product_id)
);

-- =============================================
-- Dữ liệu mẫu (Sample Data)
-- =============================================

-- Thêm danh mục sản phẩm
INSERT INTO categories (name, description, image_url) VALUES
('Laptop', 'Laptop, máy tính xách tay các loại', '/images/categories/laptop.jpg'),
('Điện thoại', 'Smartphone, điện thoại thông minh', '/images/categories/phone.jpg'),
('Tablet', 'Máy tính bảng iPad, Android tablet', '/images/categories/tablet.jpg'),
('Phụ kiện', 'Phụ kiện điện tử: tai nghe, chuột, bàn phím...', '/images/categories/accessories.jpg'),
('TV & Monitor', 'Tivi và màn hình máy tính', '/images/categories/tv-monitor.jpg'),
('Gaming', 'Thiết bị gaming: console, controller...', '/images/categories/gaming.jpg'),
('Audio', 'Thiết bị âm thanh: loa, tai nghe...', '/images/categories/audio.jpg'),
('Smart Home', 'Thiết bị nhà thông minh', '/images/categories/smart-home.jpg');

-- Thêm tài khoản admin
INSERT INTO users (full_name, email, password, role) VALUES
('Administrator', 'admin@electronicstore.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
('Manager', 'manager@electronicstore.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER'),
('Nguyễn Văn An', 'customer1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CUSTOMER'),
('Trần Thị Bình', 'customer2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CUSTOMER');

-- Thêm sản phẩm mẫu
INSERT INTO products (name, description, price, sale_price, stock_quantity, sku, brand, model, warranty_period, is_featured, category_id) VALUES
-- Laptop
('MacBook Air M2 2022 13 inch', 'MacBook Air M2 với thiết kế siêu mỏng, hiệu suất vượt trội', 28990000, 26990000, 15, 'MBA-M2-2022-13', 'Apple', 'MacBook Air M2', 12, TRUE, 1),
('Dell XPS 13 Plus', 'Laptop Dell XPS 13 Plus với thiết kế premium và hiệu suất cao', 32990000, NULL, 8, 'DELL-XPS13-PLUS', 'Dell', 'XPS 13 Plus', 24, TRUE, 1),
('ASUS ROG Strix G15', 'Laptop gaming ASUS ROG với GPU RTX 4060', 25990000, 23990000, 12, 'ASUS-ROG-G15', 'ASUS', 'ROG Strix G15', 24, TRUE, 1),
('HP Pavilion 15', 'Laptop HP Pavilion 15 cho học tập và văn phòng', 15990000, NULL, 20, 'HP-PAVILION-15', 'HP', 'Pavilion 15', 12, FALSE, 1),

-- Điện thoại
('iPhone 15 Pro Max 256GB', 'iPhone 15 Pro Max với chip A17 Pro và camera 48MP', 34990000, 32990000, 25, 'IP15-PM-256', 'Apple', 'iPhone 15 Pro Max', 12, TRUE, 2),
('Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra với S Pen và AI', 31990000, 29990000, 18, 'SGS24-ULTRA', 'Samsung', 'Galaxy S24 Ultra', 12, TRUE, 2),
('Xiaomi 14 Pro', 'Xiaomi 14 Pro với camera Leica', 19990000, NULL, 30, 'MI14-PRO', 'Xiaomi', '14 Pro', 18, FALSE, 2),
('OPPO Find X7', 'OPPO Find X7 với thiết kế premium', 22990000, 20990000, 15, 'OPPO-FX7', 'OPPO', 'Find X7', 12, FALSE, 2),

-- Tablet
('iPad Pro 12.9 M2 256GB', 'iPad Pro 12.9 inch với chip M2', 28990000, NULL, 10, 'IPADPRO-M2-256', 'Apple', 'iPad Pro 12.9', 12, TRUE, 3),
('Samsung Galaxy Tab S9 Plus', 'Galaxy Tab S9 Plus với S Pen', 21990000, 19990000, 12, 'GTABS9-PLUS', 'Samsung', 'Galaxy Tab S9+', 12, FALSE, 3),

-- Phụ kiện
('AirPods Pro 2nd Gen', 'AirPods Pro thế hệ 2 với chip H2', 5990000, NULL, 50, 'AIRPODS-PRO2', 'Apple', 'AirPods Pro 2', 12, TRUE, 4),
('Sony WH-1000XM5', 'Tai nghe chống ồn Sony WH-1000XM5', 8990000, 7990000, 25, 'SONY-WH1000XM5', 'Sony', 'WH-1000XM5', 12, TRUE, 4),
('Logitech MX Master 3S', 'Chuột không dây Logitech MX Master 3S', 2490000, NULL, 40, 'LG-MXMASTER3S', 'Logitech', 'MX Master 3S', 24, FALSE, 4),
('Keychron K2 V2', 'Bàn phím cơ không dây Keychron K2', 2890000, 2590000, 35, 'KC-K2V2', 'Keychron', 'K2 V2', 12, FALSE, 4);

-- Thêm hình ảnh sản phẩm
INSERT INTO product_images (product_id, image_url) VALUES
(1, '/images/products/macbook-air-m2-1.jpg'),
(1, '/images/products/macbook-air-m2-2.jpg'),
(2, '/images/products/dell-xps13-1.jpg'),
(3, '/images/products/asus-rog-1.jpg'),
(4, '/images/products/hp-pavilion-1.jpg'),
(5, '/images/products/iphone15-pro-max-1.jpg'),
(6, '/images/products/samsung-s24-ultra-1.jpg'),
(7, '/images/products/xiaomi-14-pro-1.jpg'),
(8, '/images/products/oppo-find-x7-1.jpg'),
(9, '/images/products/ipad-pro-m2-1.jpg'),
(10, '/images/products/galaxy-tab-s9-1.jpg'),
(11, '/images/products/airpods-pro-2-1.jpg'),
(12, '/images/products/sony-wh1000xm5-1.jpg'),
(13, '/images/products/logitech-mx-master-1.jpg'),
(14, '/images/products/keychron-k2-1.jpg');

-- Thêm đơn hàng mẫu
INSERT INTO orders (order_number, user_id, total_amount, shipping_address, phone, customer_name, status, payment_method) VALUES
('ORD001234567890', 3, 28990000, '123 Nguyễn Trãi, Q1, TP.HCM', '0901234567', 'Nguyễn Văn An', 'DELIVERED', 'BANK_TRANSFER'),
('ORD001234567891', 4, 8990000, '456 Lê Văn Sỹ, Q3, TP.HCM', '0907654321', 'Trần Thị Bình', 'SHIPPING', 'CASH_ON_DELIVERY');

-- Thêm chi tiết đơn hàng
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 1, 28990000, 28990000),
(2, 12, 1, 8990000, 8990000);

-- Thêm đánh giá sản phẩm
INSERT INTO reviews (product_id, user_id, rating, comment, is_approved) VALUES
(1, 3, 5, 'MacBook Air M2 rất tuyệt vời, pin trâu và hiệu suất mạnh mẽ!', TRUE),
(12, 4, 4, 'Tai nghe Sony chống ồn tốt, chất lượng âm thanh tuyệt vời.', TRUE);

-- =============================================
-- Views và Procedures hữu ích
-- =============================================

-- View: Sản phẩm với thông tin danh mục
CREATE VIEW product_details AS
SELECT 
    p.*,
    c.name as category_name,
    c.description as category_description,
    (SELECT GROUP_CONCAT(image_url) FROM product_images pi WHERE pi.product_id = p.id) as image_urls,
    COALESCE(p.sale_price, p.price) as current_price,
    CASE WHEN p.sale_price IS NOT NULL AND p.sale_price > 0 THEN TRUE ELSE FALSE END as is_on_sale
FROM products p 
JOIN categories c ON p.category_id = c.id;

-- View: Thống kê đơn hàng
CREATE VIEW order_statistics AS
SELECT 
    DATE(created_at) as order_date,
    COUNT(*) as total_orders,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_order_value
FROM orders 
WHERE status = 'DELIVERED'
GROUP BY DATE(created_at);

-- View: Sản phẩm bán chạy
CREATE VIEW best_selling_products AS
SELECT 
    p.id,
    p.name,
    p.price,
    SUM(oi.quantity) as total_sold,
    SUM(oi.total_price) as total_revenue
FROM products p
JOIN order_items oi ON p.id = oi.product_id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'DELIVERED'
GROUP BY p.id, p.name, p.price
ORDER BY total_sold DESC;

-- =============================================
-- Indexes for Performance
-- =============================================

-- Tạo full-text index cho tìm kiếm sản phẩm
CREATE FULLTEXT INDEX idx_products_fulltext ON products(name, description);

-- Composite indexes
CREATE INDEX idx_products_category_active ON products(category_id, is_active);
CREATE INDEX idx_products_featured_active ON products(is_featured, is_active);
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_date_status ON orders(created_at, status);

-- =============================================
-- Stored Procedures
-- =============================================

DELIMITER //

-- Procedure: Cập nhật tồn kho sau khi đặt hàng
CREATE PROCEDURE UpdateProductStock(
    IN p_product_id BIGINT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_current_stock INT;
    DECLARE v_new_stock INT;
    
    -- Lấy số lượng tồn kho hiện tại
    SELECT stock_quantity INTO v_current_stock 
    FROM products 
    WHERE id = p_product_id;
    
    -- Tính số lượng mới
    SET v_new_stock = v_current_stock - p_quantity;
    
    -- Kiểm tra có đủ hàng không
    IF v_new_stock < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Không đủ hàng trong kho';
    ELSE
        -- Cập nhật tồn kho
        UPDATE products 
        SET stock_quantity = v_new_stock,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = p_product_id;
    END IF;
END //

-- Function: Tính tổng giá trị đơn hàng
CREATE FUNCTION CalculateOrderTotal(p_order_id BIGINT)
RETURNS DECIMAL(12,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_total DECIMAL(12,2) DEFAULT 0;
    
    SELECT COALESCE(SUM(total_price), 0) INTO v_total
    FROM order_items
    WHERE order_id = p_order_id;
    
    RETURN v_total;
END //

DELIMITER ;

-- =============================================
-- Triggers
-- =============================================

DELIMITER //

-- Trigger: Tự động cập nhật updated_at
CREATE TRIGGER tr_products_update_timestamp
    BEFORE UPDATE ON products
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

CREATE TRIGGER tr_categories_update_timestamp
    BEFORE UPDATE ON categories
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

CREATE TRIGGER tr_users_update_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

DELIMITER ;

-- =============================================
-- Sample Queries for Testing
-- =============================================

-- Lấy sản phẩm nổi bật
-- SELECT * FROM product_details WHERE is_featured = TRUE AND is_active = TRUE ORDER BY created_at DESC LIMIT 8;

-- Tìm kiếm sản phẩm
-- SELECT * FROM products WHERE MATCH(name, description) AGAINST('laptop gaming' IN NATURAL LANGUAGE MODE);

-- Thống kê doanh thu theo ngày
-- SELECT * FROM order_statistics ORDER BY order_date DESC LIMIT 30;

-- Sản phẩm bán chạy nhất
-- SELECT * FROM best_selling_products LIMIT 10;

COMMIT;