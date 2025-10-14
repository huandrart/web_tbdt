-- Migration: Add shipper support
-- Add shipper_id column to orders table
ALTER TABLE orders ADD COLUMN shipper_id BIGINT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_shipper 
    FOREIGN KEY (shipper_id) REFERENCES users(id);

-- Create shipper_reviews table
CREATE TABLE shipper_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    shipper_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_shipper_reviews_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_shipper_reviews_shipper 
        FOREIGN KEY (shipper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shipper_reviews_customer 
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_shipper_reviews_order (order_id),
    INDEX idx_shipper_reviews_shipper (shipper_id),
    INDEX idx_shipper_reviews_customer (customer_id),
    INDEX idx_shipper_reviews_rating (rating)
);

-- Add some sample shipper users (optional)
-- You can uncomment these lines to create sample shipper accounts
-- INSERT INTO users (full_name, email, password, phone, address, role, is_active, created_at) VALUES
-- ('Nguyễn Văn A', 'shipper1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'SHIPPER', 1, NOW()),
-- ('Trần Thị B', 'shipper2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0987654321', '456 Đường XYZ, Quận 2, TP.HCM', 'SHIPPER', 1, NOW());
