# Tách CSS và JavaScript ra khỏi Templates

## Tổng quan

Đã tách tất cả CSS và JavaScript ra khỏi các template HTML và đưa vào các file riêng biệt trong thư mục `resources/static` để dễ quản lý và bảo trì.

## Cấu trúc thư mục

```
src/main/resources/static/
├── css/
│   ├── admin-category-view.css    # CSS cho trang view category
│   ├── admin-category-list.css    # CSS cho trang list category
│   └── register.css               # CSS khác (đã có sẵn)
├── js/
│   ├── admin-category-view.js     # JavaScript cho trang view category
│   └── admin-category-list.js     # JavaScript cho trang list category
└── images/
    └── ...                        # Các hình ảnh
```

## Files đã tạo

### 1. ✅ CSS Files

#### `admin-category-view.css`
- Styles cho trang xem chi tiết danh mục
- Sidebar, main content, category cards
- Product items với ảnh và thông tin
- Button styles (danger-modern, primary-modern)
- Status badges (active/inactive)
- Product image containers với hover effects
- Responsive design

#### `admin-category-list.css`
- Styles cho trang danh sách danh mục
- Sidebar, main content, category cards
- Table styles với hover effects
- Search container và filter buttons
- Pagination container
- Responsive adjustments

### 2. ✅ JavaScript Files

#### `admin-category-view.js`
- Function `confirmDeleteCategory()` - Xác nhận xóa danh mục
- Function `initializeProductImages()` - Khởi tạo ảnh sản phẩm
- Function `initializeTooltips()` - Khởi tạo Bootstrap tooltips
- Utility functions: `formatCurrency()`, `formatDate()`
- Error handling cho ảnh sản phẩm

#### `admin-category-list.js`
- Function `confirmDeleteCategory()` - Xác nhận xóa danh mục
- Function `initializeToggleStatus()` - Toggle trạng thái danh mục
- Function `initializeSearch()` - Tìm kiếm với debounce
- Function `initializeFilters()` - Filter buttons
- Function `showAlert()` - Hiển thị thông báo
- AJAX calls cho toggle status

## Cách sử dụng trong Templates

### 1. ✅ CSS
```html
<!-- Custom CSS -->
<link th:href="@{/css/admin-category-view.css}" rel="stylesheet">
<link th:href="@{/css/admin-category-list.css}" rel="stylesheet">
```

### 2. ✅ JavaScript
```html
<!-- Custom JavaScript -->
<script th:src="@{/js/admin-category-view.js}"></script>
<script th:src="@{/js/admin-category-list.js}"></script>
```

## Lợi ích

### ✅ **Tách biệt concerns**
- HTML: Cấu trúc và nội dung
- CSS: Giao diện và styling
- JavaScript: Logic và tương tác

### ✅ **Dễ bảo trì**
- Code được tổ chức rõ ràng
- Dễ tìm và sửa lỗi
- Có thể tái sử dụng CSS/JS

### ✅ **Performance tốt hơn**
- Browser có thể cache CSS/JS files
- Giảm kích thước HTML
- Load song song CSS/JS

### ✅ **Code reusability**
- CSS có thể dùng cho nhiều trang
- JavaScript functions có thể import
- Dễ chia sẻ giữa các components

### ✅ **Development experience**
- Syntax highlighting tốt hơn
- IntelliSense/autocomplete
- Debugging dễ dàng hơn

## Cập nhật Templates

### ✅ `admin/categories/view.html`
- Loại bỏ toàn bộ `<style>` tag
- Loại bỏ toàn bộ `<script>` tag
- Thêm link đến CSS file
- Thêm script tag đến JS file

### ✅ `admin/categories/list.html`
- Loại bỏ toàn bộ `<style>` tag
- Loại bỏ toàn bộ `<script>` tag
- Thêm link đến CSS file
- Thêm script tag đến JS file

## Best Practices

### ✅ **Naming Convention**
- CSS: `admin-{page}-{action}.css`
- JS: `admin-{page}-{action}.js`
- Ví dụ: `admin-category-view.css`

### ✅ **Organization**
- Mỗi trang có CSS/JS riêng
- Common styles có thể tách ra file riêng
- Utility functions trong JS files

### ✅ **Performance**
- Minify CSS/JS trong production
- Use CDN cho external libraries
- Lazy load non-critical JS

## Lưu ý

- ✅ **Thymeleaf syntax**: Sử dụng `th:href="@{/css/...}"` và `th:src="@{/js/...}"`
- ✅ **Cache busting**: Có thể thêm version parameter nếu cần
- ✅ **Error handling**: JavaScript có error handling cho AJAX calls
- ✅ **Responsive**: CSS có media queries cho mobile
- ✅ **Accessibility**: Giữ nguyên các ARIA attributes và semantic HTML
