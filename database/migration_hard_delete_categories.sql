-- Migration script để hỗ trợ hard delete categories
-- Khi xóa category, products sẽ có category_id = NULL

USE electronic_store_db;

-- Cập nhật foreign key constraint để cho phép xóa category
-- và set category_id của products thành NULL
ALTER TABLE products 
DROP FOREIGN KEY fk_products_category;

-- Làm cho category_id có thể NULL
ALTER TABLE products 
MODIFY COLUMN category_id BIGINT NULL;

-- Tạo lại foreign key với ON DELETE SET NULL
ALTER TABLE products 
ADD CONSTRAINT fk_products_category 
FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

-- Kiểm tra các categories hiện có
SELECT 
    id,
    name,
    is_active,
    created_at
FROM categories 
ORDER BY id;

-- Lưu ý: Sau khi chạy migration này:
-- 1. Khi xóa category, ID sẽ được giải phóng
-- 2. Products thuộc category bị xóa sẽ có category_id = NULL
-- 3. Có thể tạo category mới với ID đã bị xóa
