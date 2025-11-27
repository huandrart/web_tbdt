# 📋 Tài liệu Luồng Hoạt Động của User

## 🎯 Tổng Quan

Tài liệu này mô tả chi tiết các luồng hoạt động của người dùng (USER) trong hệ thống Electronic Store.

---

## 🔐 1. LUỒNG XÁC THỰC (Authentication Flow)

### 1.1. Đăng Ký (Registration)

**Controller:** `AuthController.java`
**Endpoint:** `POST /register`

**Luồng hoạt động:**
```
1. User truy cập /register
   └─> GET /register → Hiển thị form đăng ký (auth/register.html)

2. User điền thông tin và submit
   └─> POST /register
       ├─> Kiểm tra mật khẩu xác nhận
       ├─> Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
       └─> Gọi UserService.createUser()
           ├─> Kiểm tra email đã tồn tại
           ├─> Mã hóa mật khẩu bằng BCrypt
           ├─> Tạo verification token (UUID)
           ├─> Set emailVerified = false
           ├─> Set isActive = false (khóa tài khoản)
           ├─> Lưu user vào database
           └─> Gửi email xác thực
               └─> EmailService.sendVerificationEmail()
                   └─> Link: http://localhost:8080/verify-email?token={token}

3. Redirect → /email-verification
   └─> Hiển thị trang thông báo kiểm tra email
```

**Files liên quan:**
- `AuthController.java` (dòng 23-58)
- `UserService.java` (dòng 74-112)
- `EmailService.java` (dòng 24-60)
- `application.properties` (cấu hình email)

---

### 1.2. Xác Thực Email (Email Verification)

**Controller:** `AuthController.java`
**Endpoint:** `GET /verify-email?token={token}`

**Luồng hoạt động:**
```
1. User click link trong email
   └─> GET /verify-email?token={token}
       └─> UserService.verifyEmail(token)
           ├─> Tìm user theo verification token
           ├─> Kiểm tra token chưa hết hạn
           ├─> Set emailVerified = true
           ├─> Set isActive = true (kích hoạt tài khoản)
           ├─> Xóa verification token
           └─> Lưu vào database

2. Redirect → /login
   └─> Hiển thị thông báo thành công/thất bại
```

**Files liên quan:**
- `AuthController.java` (dòng 155-173)
- `UserService.java` (dòng 260-284)

---

### 1.3. Đăng Nhập (Login)

**Controller:** Spring Security (formLogin)
**Endpoint:** `POST /perform_login`

**Luồng hoạt động:**
```
1. User truy cập /login
   └─> GET /login → Hiển thị form đăng nhập (auth/login.html)

2. User nhập email và password
   └─> POST /perform_login
       ├─> Spring Security xác thực
       │   └─> UserService.loadUserByUsername(email)
       │       ├─> Tìm user theo email
       │       ├─> Kiểm tra isActive = true
       │       └─> Kiểm tra emailVerified = true
       │
       └─> Redirect theo role:
           ├─> ADMIN/SUPER_ADMIN → /admin/dashboard
           ├─> SHIPPER → /shipper
           └─> USER → /home
```

**Files liên quan:**
- `SecurityConfig.java` (dòng 35-57)
- `UserService.java` (loadUserByUsername method)

---

### 1.4. Quên Mật Khẩu (Forgot Password)

**Controller:** `AuthController.java`
**Endpoint:** `POST /forgot-password`

**Luồng hoạt động:**
```
1. User truy cập /forgot-password
   └─> GET /forgot-password → Hiển thị form (auth/forgot-password.html)

2. User nhập email
   └─> POST /forgot-password
       └─> UserService.generatePasswordResetToken(email)
           ├─> Tìm user theo email
           ├─> Tạo reset token (UUID)
           ├─> Set resetTokenExpires (1 giờ)
           └─> Gửi email reset password
               └─> EmailService.sendPasswordResetEmail()
                   └─> Link: http://localhost:8080/reset-password?token={token}

3. Redirect → /login
```

**Files liên quan:**
- `AuthController.java` (dòng 70-90, 92-112, 114-153)
- `UserService.java` (generatePasswordResetToken, resetPassword methods)

---

## 🏠 2. LUỒNG DUYỆT SẢN PHẨM (Product Browsing Flow)

### 2.1. Trang Chủ

**Controller:** `HomeController.java`
**Endpoint:** `GET /` hoặc `GET /home`

**Luồng hoạt động:**
```
1. User truy cập trang chủ
   └─> GET / hoặc /home
       ├─> Lấy 8 sản phẩm nổi bật (featured)
       ├─> Lấy 8 sản phẩm phổ biến (popular)
       └─> Lấy danh sách danh mục có sản phẩm
           └─> Hiển thị index.html
```

**Files liên quan:**
- `HomeController.java` (dòng 32-56)

---

### 2.2. Danh Sách Sản Phẩm

**Controller:** `HomeController.java`
**Endpoint:** `GET /products`

**Luồng hoạt động:**
```
1. User truy cập /products
   └─> GET /products
       ├─> Hỗ trợ filter:
       │   ├─> categoryId: Lọc theo danh mục
       │   ├─> keyword: Tìm kiếm theo tên
       │   ├─> minPrice/maxPrice: Lọc theo giá
       │   └─> brand: Lọc theo thương hiệu
       │
       ├─> Phân trang (mặc định 12 sản phẩm/trang)
       └─> Hiển thị products.html
```

**Files liên quan:**
- `HomeController.java` (dòng 84-134)

---

### 2.3. Chi Tiết Sản Phẩm

**Controller:** `HomeController.java`
**Endpoint:** `GET /product/{id}`

**Luồng hoạt động:**
```
1. User click vào sản phẩm
   └─> GET /product/{id}
       ├─> Lấy thông tin sản phẩm
       ├─> Tăng view count
       ├─> Lấy 4 sản phẩm liên quan (cùng danh mục)
       ├─> Lấy tất cả reviews của sản phẩm
       ├─> Tính điểm đánh giá trung bình
       └─> Hiển thị product/detail.html
```

**Files liên quan:**
- `HomeController.java` (dòng 136-164)

---

### 2.4. Sản Phẩm Theo Danh Mục

**Controller:** `HomeController.java`
**Endpoint:** `GET /category/{id}`

**Luồng hoạt động:**
```
1. User click vào danh mục
   └─> GET /category/{id}
       ├─> Lấy thông tin danh mục
       ├─> Lấy sản phẩm thuộc danh mục (phân trang)
       └─> Hiển thị category-products.html
```

**Files liên quan:**
- `HomeController.java` (dòng 166-185)

---

## 🛒 3. LUỒNG GIỎ HÀNG (Shopping Cart Flow)

### 3.1. Thêm Vào Giỏ Hàng

**Controller:** `CartController.java`
**Endpoint:** `POST /cart/add`

**Luồng hoạt động:**
```
1. User click "Thêm vào giỏ hàng"
   └─> POST /cart/add?productId={id}&quantity={qty}
       ├─> Kiểm tra đăng nhập
       ├─> Lấy user từ authentication
       ├─> Kiểm tra sản phẩm tồn tại và đang bán
       ├─> Kiểm tra số lượng tồn kho
       └─> CartService.addToCart(user, product, quantity)
           └─> Lưu vào bảng carts trong database
       
2. Response JSON: {"success": true/false, "message": "..."}
```

**Files liên quan:**
- `CartController.java` (dòng 85-127)
- `CartService.java`

---

### 3.2. Xem Giỏ Hàng

**Controller:** `CartController.java`
**Endpoint:** `GET /cart`

**Luồng hoạt động:**
```
1. User truy cập /cart
   └─> GET /cart
       ├─> Kiểm tra đăng nhập
       ├─> Lấy user từ authentication
       ├─> CartService.findByUser(user)
       │   └─> Lấy tất cả items trong giỏ hàng
       ├─> Tính tổng tiền
       └─> Hiển thị cart/cart.html
```

**Files liên quan:**
- `CartController.java` (dòng 38-63)

---

### 3.3. Cập Nhật Số Lượng

**Controller:** `CartController.java`
**Endpoint:** `POST /cart/update`

**Luồng hoạt động:**
```
1. User thay đổi số lượng
   └─> POST /cart/update?productId={id}&quantity={qty}
       ├─> Kiểm tra đăng nhập
       ├─> Kiểm tra số lượng > 0
       ├─> Kiểm tra số lượng <= tồn kho
       └─> CartService.updateQuantity(user, product, quantity)
       
2. Response JSON: {"success": true/false, "message": "..."}
```

**Files liên quan:**
- `CartController.java` (dòng 129-171)

---

### 3.4. Xóa Sản Phẩm Khỏi Giỏ Hàng

**Controller:** `CartController.java`
**Endpoint:** `POST /cart/remove`

**Luồng hoạt động:**
```
1. User click "Xóa"
   └─> POST /cart/remove?productId={id}
       ├─> Kiểm tra đăng nhập
       └─> CartService.removeFromCart(user, product)
           └─> Xóa record khỏi bảng carts
       
2. Response JSON: {"success": true/false, "message": "..."}
```

**Files liên quan:**
- `CartController.java` (dòng 173-206)

---

### 3.5. Xóa Tất Cả Giỏ Hàng

**Controller:** `CartController.java`
**Endpoint:** `POST /cart/clear`

**Luồng hoạt động:**
```
1. User click "Xóa tất cả"
   └─> POST /cart/clear
       ├─> Kiểm tra đăng nhập
       └─> CartService.clearCart(user)
           └─> Xóa tất cả records của user khỏi bảng carts
       
2. Response JSON: {"success": true/false, "message": "..."}
```

**Files liên quan:**
- `CartController.java` (dòng 208-235)

---

## 💳 4. LUỒNG THANH TOÁN (Checkout Flow)

### 4.1. Trang Thanh Toán

**Controller:** `CheckoutController.java`
**Endpoint:** `GET /checkout`

**Luồng hoạt động:**
```
1. User click "Thanh toán" từ giỏ hàng
   └─> GET /checkout
       ├─> Kiểm tra đăng nhập
       ├─> Lấy giỏ hàng từ database
       ├─> Kiểm tra giỏ hàng không rỗng
       ├─> Tính toán:
       │   ├─> Subtotal: Tổng giá sản phẩm
       │   ├─> Shipping fee: 30,000 VND
       │   └─> Total: Subtotal + Shipping
       └─> Hiển thị checkout/checkout.html
```

**Files liên quan:**
- `CheckoutController.java` (dòng 40-79)

---

### 4.2. Xử Lý Đặt Hàng

**Controller:** `CheckoutController.java`
**Endpoint:** `POST /checkout/process`

**Luồng hoạt động:**
```
1. User điền thông tin và submit
   └─> POST /checkout/process
       ├─> Validation:
       │   ├─> customerName không rỗng
       │   ├─> phone không rỗng
       │   └─> address không rỗng
       │
       ├─> Lấy giỏ hàng từ database
       ├─> Kiểm tra tồn kho cho từng sản phẩm
       ├─> Tạo Order:
       │   ├─> Set thông tin user, địa chỉ, phone
       │   ├─> Set status = PENDING
       │   ├─> Set paymentStatus = PENDING
       │   ├─> Set paymentMethod (COD, BANK_TRANSFER, E_WALLET, CREDIT_CARD)
       │   └─> Tính totalAmount
       │
       ├─> Tạo OrderItems từ CartItems
       ├─> Lưu Order và OrderItems vào database
       ├─> Xóa giỏ hàng (CartService.clearCart)
       │
       └─> Redirect theo payment method:
           ├─> E_WALLET → /payment/momo?orderId={id}&totalAmount={amount}
           └─> Khác → /payment/success/{orderId}
```

**Files liên quan:**
- `CheckoutController.java` (dòng 81-214)
- `OrderService.java`

---

## 📦 5. LUỒNG QUẢN LÝ ĐƠN HÀNG (Order Management Flow)

### 5.1. Xem Danh Sách Đơn Hàng

**Controller:** `UserOrderController.java`
**Endpoint:** `GET /orders` hoặc `GET /orders/my-orders`

**Luồng hoạt động:**
```
1. User truy cập /orders
   └─> GET /orders
       ├─> Kiểm tra đăng nhập
       ├─> Lấy user từ authentication
       ├─> Hỗ trợ filter theo status (optional)
       ├─> Phân trang (mặc định 10 đơn/trang)
       ├─> Sắp xếp theo createdAt DESC
       └─> Hiển thị orders/my-orders.html
```

**Files liên quan:**
- `UserOrderController.java` (dòng 32-67)

---

### 5.2. Xem Chi Tiết Đơn Hàng

**Controller:** `UserOrderController.java`
**Endpoint:** `GET /orders/{id}`

**Luồng hoạt động:**
```
1. User click vào đơn hàng
   └─> GET /orders/{id}
       ├─> Kiểm tra đăng nhập
       ├─> Lấy đơn hàng từ database
       ├─> Kiểm tra đơn hàng thuộc về user hiện tại
       └─> Hiển thị orders/order-detail.html
```

**Files liên quan:**
- `UserOrderController.java` (dòng 70-91)

---

### 5.3. Hủy Đơn Hàng

**Controller:** `UserOrderController.java`
**Endpoint:** `POST /orders/{id}/cancel`

**Luồng hoạt động:**
```
1. User click "Hủy đơn hàng"
   └─> POST /orders/{id}/cancel
       ├─> Kiểm tra đăng nhập
       ├─> Lấy đơn hàng từ database
       ├─> Kiểm tra đơn hàng thuộc về user hiện tại
       ├─> Kiểm tra status = PENDING (chỉ cho phép hủy đơn PENDING)
       └─> OrderService.cancelOrder(id)
           └─> Set status = CANCELLED
       
2. Redirect → /orders
```

**Files liên quan:**
- `UserOrderController.java` (dòng 94-125)
- `OrderService.java` (cancelOrder method)

---

## 👤 6. LUỒNG QUẢN LÝ HỒ SƠ (Profile Management Flow)

### 6.1. Xem Hồ Sơ

**Controller:** `ProfileController.java`
**Endpoint:** `GET /profile`

**Luồng hoạt động:**
```
1. User truy cập /profile
   └─> GET /profile
       ├─> Kiểm tra đăng nhập
       ├─> Lấy user từ authentication
       └─> Hiển thị profile/profile.html
```

**Files liên quan:**
- `ProfileController.java` (dòng 25-34)

---

### 6.2. Chỉnh Sửa Hồ Sơ

**Controller:** `ProfileController.java`
**Endpoint:** `GET /profile/edit` và `POST /profile/edit`

**Luồng hoạt động:**
```
1. User click "Chỉnh sửa"
   └─> GET /profile/edit
       └─> Hiển thị form edit-profile.html

2. User cập nhật thông tin và submit
   └─> POST /profile/edit
       ├─> Cập nhật: fullName, firstName, lastName
       ├─> Giữ nguyên: email, password
       └─> UserService.update(user)
           └─> Lưu vào database
       
3. Redirect → /profile?success=true
```

**Files liên quan:**
- `ProfileController.java` (dòng 37-84)

---

### 6.3. Đổi Mật Khẩu

**Controller:** `ProfileController.java`
**Endpoint:** `GET /profile/change-password` và `POST /profile/change-password`

**Luồng hoạt động:**
```
1. User click "Đổi mật khẩu"
   └─> GET /profile/change-password
       └─> Hiển thị form change-password.html

2. User nhập mật khẩu và submit
   └─> POST /profile/change-password
       ├─> Validation:
       │   ├─> Mật khẩu hiện tại đúng
       │   ├─> Mật khẩu mới và xác nhận khớp
       │   └─> Mật khẩu mới >= 8 ký tự
       │
       ├─> Mã hóa mật khẩu mới bằng BCrypt
       └─> UserService.update(user)
           └─> Lưu vào database
       
3. Redirect → /profile/change-password?success=true
```

**Files liên quan:**
- `ProfileController.java` (dòng 87-138)

---

## 🔒 7. BẢO MẬT VÀ PHÂN QUYỀN

### 7.1. Security Configuration

**File:** `SecurityConfig.java`

**Các endpoint công khai (không cần đăng nhập):**
- `/`, `/home`, `/products/**`, `/categories/**`
- `/register`, `/login`, `/forgot-password`, `/reset-password`
- `/verify-email`, `/resend-verification`, `/email-verification`
- `/about`, `/contact`
- `/cart/**` (có thể xem, nhưng cần đăng nhập để thêm vào giỏ)

**Các endpoint yêu cầu đăng nhập (USER role):**
- `/profile/**` - Quản lý hồ sơ
- `/orders/**` - Quản lý đơn hàng
- `/reviews/**` - Đánh giá sản phẩm
- `/order/**` - Đặt hàng

**Các endpoint yêu cầu ADMIN:**
- `/admin/**` - Quản trị hệ thống

**Các endpoint yêu cầu SHIPPER:**
- `/shipper/**` - Quản lý giao hàng

---

## 📊 8. SƠ ĐỒ LUỒNG TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────┐
│                    USER JOURNEY OVERVIEW                    │
└─────────────────────────────────────────────────────────────┘

1. ĐĂNG KÝ & XÁC THỰC
   Register → Email Verification → Login → Home

2. DUYỆT SẢN PHẨM
   Home → Products → Product Detail → Add to Cart

3. MUA SẮM
   Cart → Update/Remove Items → Checkout → Place Order

4. QUẢN LÝ ĐƠN HÀNG
   Orders List → Order Detail → Cancel (if PENDING)

5. QUẢN LÝ HỒ SƠ
   Profile → Edit Profile / Change Password

6. ĐÁNH GIÁ (Optional)
   Order Detail → Review Product
```

---

## 🔗 9. CÁC FILE QUAN TRỌNG

### Controllers
- `AuthController.java` - Xác thực (đăng ký, đăng nhập, quên mật khẩu)
- `HomeController.java` - Trang chủ, sản phẩm, danh mục
- `CartController.java` - Quản lý giỏ hàng
- `CheckoutController.java` - Thanh toán
- `UserOrderController.java` - Quản lý đơn hàng của user
- `ProfileController.java` - Quản lý hồ sơ

### Services
- `UserService.java` - Logic nghiệp vụ user
- `EmailService.java` - Gửi email
- `CartService.java` - Logic giỏ hàng
- `OrderService.java` - Logic đơn hàng
- `ProductService.java` - Logic sản phẩm

### Configuration
- `SecurityConfig.java` - Cấu hình bảo mật và phân quyền
- `application.properties` - Cấu hình ứng dụng

---

## 📝 10. GHI CHÚ QUAN TRỌNG

1. **Email Verification**: User phải xác thực email trước khi có thể đăng nhập
2. **Cart Persistence**: Giỏ hàng được lưu trong database, không mất khi đăng xuất
3. **Order Status**: Chỉ có thể hủy đơn hàng ở trạng thái PENDING
4. **Stock Check**: Hệ thống kiểm tra tồn kho khi thêm vào giỏ và khi đặt hàng
5. **Payment Methods**: Hỗ trợ COD, Bank Transfer, E-Wallet, Credit Card
6. **Pagination**: Hầu hết danh sách đều có phân trang

---

**Tài liệu được tạo tự động dựa trên phân tích codebase**
**Cập nhật lần cuối:** 2024

