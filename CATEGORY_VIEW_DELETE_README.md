# Tính năng View và Hard Delete Categories

## Tổng quan

Đã hoàn thiện tính năng xem chi tiết danh mục (view) và xóa hoàn toàn danh mục (hard delete) với giao diện đẹp và thông báo xác nhận chi tiết.

## Tính năng đã implement

### 1. ✅ View Category (Xem chi tiết danh mục)

**Controller:**
- `AdminCategoryController.viewCategory()` - Hiển thị chi tiết danh mục

**Service:**
- `CategoryBusinessService.getCategoryWithDetails()` - Lấy danh mục với đầy đủ thông tin
- `CategoryBusinessServiceImpl.getCategoryWithDetails()` - Implementation

**Template:**
- `admin/categories/view.html` - Giao diện xem chi tiết đẹp mắt

**Thông tin hiển thị:**
- ✅ Tên danh mục
- ✅ Mô tả
- ✅ Trạng thái (Hoạt động/Tạm dừng)
- ✅ Hình ảnh danh mục
- ✅ Ngày tạo và cập nhật
- ✅ Danh sách sản phẩm thuộc danh mục
- ✅ Nút chỉnh sửa và xóa

### 2. ✅ Hard Delete Category (Xóa hoàn toàn danh mục)

**Service:**
- `CategoryService.hardDeleteById()` - Xóa hẳn khỏi database
- `CategoryBusinessServiceImpl.deleteCategory()` - Xóa với kiểm tra ràng buộc

**Database:**
- Foreign key constraint: `ON DELETE SET NULL`
- Products sẽ có `category_id = NULL` khi danh mục bị xóa

**UI:**
- Thông báo xác nhận chi tiết với cảnh báo
- Hiển thị số lượng sản phẩm sẽ bị ảnh hưởng

## Cách hoạt động

### Khi xem danh mục:
1. Truy cập `/admin/categories/view/{id}`
2. Hiển thị đầy đủ thông tin danh mục
3. Hiển thị danh sách sản phẩm thuộc danh mục
4. Có nút chỉnh sửa và xóa

### Khi xóa danh mục:
1. Kiểm tra có sản phẩm liên quan không
2. Hiển thị thông báo xác nhận chi tiết
3. Xóa file hình ảnh (nếu có)
4. Xóa hẳn danh mục khỏi database
5. Sản phẩm liên quan có `category_id = NULL`

## Files đã tạo/cập nhật

### Controllers:
- ✅ `AdminCategoryController.java` - Thêm method `viewCategory()`

### Services:
- ✅ `CategoryBusinessService.java` - Thêm method `getCategoryWithDetails()`
- ✅ `CategoryBusinessServiceImpl.java` - Implement các method mới
- ✅ `CategoryService.java` - Thêm method `hardDeleteById()`

### Entities:
- ✅ `Product.java` - Cho phép `category_id` có thể NULL

### Templates:
- ✅ `admin/categories/view.html` - Template xem chi tiết mới
- ✅ `admin/categories/list.html` - Cập nhật thông báo xóa

### Database:
- ✅ `electronic_store_schema.sql` - Cập nhật foreign key constraint
- ✅ `migration_hard_delete_categories.sql` - Migration script

## Lợi ích

✅ **Giao diện đẹp**: Template view hiện đại với Bootstrap 5
✅ **Thông tin đầy đủ**: Hiển thị tất cả thông tin cần thiết
✅ **An toàn**: Kiểm tra ràng buộc trước khi xóa
✅ **Thông báo rõ ràng**: Cảnh báo chi tiết về hậu quả
✅ **Hard delete**: Xóa hẳn khỏi database
✅ **Tự động xử lý**: Products tự động có category_id = NULL

## Cách sử dụng

1. **Chạy migration**:
   ```sql
   source database/migration_hard_delete_categories.sql
   ```

2. **Xem danh mục**:
   - Truy cập danh sách categories
   - Click nút "Xem" hoặc "Chi tiết"

3. **Xóa danh mục**:
   - Từ danh sách hoặc trang chi tiết
   - Click nút "Xóa danh mục"
   - Xác nhận trong popup

## Lưu ý

- ⚠️ **Không thể hoàn tác**: Sau khi xóa, danh mục mất vĩnh viễn
- ⚠️ **Sản phẩm bị ảnh hưởng**: Sản phẩm sẽ không còn thuộc danh mục nào
- ✅ **ID được giải phóng**: Có thể tạo danh mục mới với ID đã xóa
- ✅ **File hình ảnh**: Tự động bị xóa khỏi hệ thống
