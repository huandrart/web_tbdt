# Electronic Store - Website Bán Thiết Bị Điện Tử

Một website bán thiết bị điện tử được xây dựng bằng Spring Boot, MySQL và Thymeleaf.

## 🚀 Tính năng chính

### Khách hàng
- 🏠 **Trang chủ**: Hiển thị sản phẩm nổi bật, phổ biến và danh mục
- 🔍 **Tìm kiếm & Lọc**: Tìm kiếm theo tên, lọc theo danh mục, thương hiệu, giá
- 📱 **Chi tiết sản phẩm**: Xem thông tin chi tiết, hình ảnh, đánh giá
- 🛒 **Giỏ hàng**: Thêm, sửa, xóa sản phẩm trong giỏ
- 📋 **Đặt hàng**: Thông tin giao hàng, thanh toán
- 👤 **Tài khoản**: Đăng ký, đăng nhập, quản lý thông tin
- ⭐ **Đánh giá**: Đánh giá và nhận xét sản phẩm

### Quản trị viên
- 📊 **Dashboard**: Thống kê doanh thu, đơn hàng, sản phẩm
- 📦 **Quản lý sản phẩm**: CRUD sản phẩm, danh mục
- 📋 **Quản lý đơn hàng**: Xem, cập nhật trạng thái đơn hàng
- 👥 **Quản lý khách hàng**: Xem danh sách, thông tin khách hàng
- 💬 **Quản lý đánh giá**: Duyệt và quản lý đánh giá sản phẩm

## 🛠 Công nghệ sử dụng

### Backend
- **Java 17**
- **Spring Boot 3.1.0**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
- **MySQL 8.0**
- **Hibernate**

### Frontend
- **Thymeleaf** - Template engine
- **Bootstrap 5.3** - CSS Framework
- **Font Awesome 6.4** - Icons
- **jQuery 3.6** - JavaScript

### Tools & Others
- **Maven** - Build tool
- **Spring Boot DevTools** - Development
- **XAMPP** - Local MySQL server

## 📋 Yêu cầu hệ thống

- **Java 17+**
- **Maven 3.6+**
- **MySQL 8.0+** (hoặc XAMPP)
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code

## 🚀 Cài đặt và chạy dự án

### Bước 1: Cài đặt XAMPP
1. Tải và cài đặt [XAMPP](https://www.apachefriends.org/)
2. Khởi động **Apache** và **MySQL** trong XAMPP Control Panel
3. Truy cập [http://localhost/phpmyadmin](http://localhost/phpmyadmin)

### Bước 2: Tạo cơ sở dữ liệu
1. Mở phpMyAdmin
2. Tạo database mới tên `electronic_store_db`
3. Import file SQL `database/electronic_store_schema.sql` hoặc chạy script SQL

```sql
CREATE DATABASE electronic_store_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### Bước 3: Cấu hình kết nối database
Kiểm tra file `src/main/resources/application.properties`:

```properties
# Database Configuration (XAMPP MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/electronic_store_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Bước 4: Clone và build dự án
```bash
# Clone dự án (nếu có Git repository)
git clone [repository-url]
cd electronic-store

# Build dự án
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### Bước 5: Truy cập ứng dụng
- **Website**: [http://localhost:8080](http://localhost:8080)
- **Database**: [http://localhost/phpmyadmin](http://localhost/phpmyadmin)

## 📊 Cấu trúc cơ sở dữ liệu

### Bảng chính
- **categories**: Danh mục sản phẩm
- **products**: Sản phẩm
- **product_images**: Hình ảnh sản phẩm
- **users**: Người dùng (khách hàng, admin)
- **orders**: Đơn hàng
- **order_items**: Chi tiết đơn hàng
- **reviews**: Đánh giá sản phẩm

### ERD (Entity Relationship Diagram)
```
Categories (1) -----> (N) Products
Users (1) -----> (N) Orders
Users (1) -----> (N) Reviews
Products (1) -----> (N) Reviews
Products (1) -----> (N) Product_Images
Products (1) -----> (N) Order_Items
Orders (1) -----> (N) Order_Items
```

## 🗂 Cấu trúc thư mục

```
electronic-store/
├── src/
│   ├── main/
│   │   ├── java/com/electronicstore/
│   │   │   ├── controller/          # Controllers
│   │   │   ├── entity/             # Entity classes
│   │   │   ├── repository/         # JPA Repositories
│   │   │   ├── service/            # Business logic
│   │   │   ├── config/             # Configuration
│   │   │   └── ElectronicStoreApplication.java
│   │   └── resources/
│   │       ├── templates/          # Thymeleaf templates
│   │       ├── static/             # CSS, JS, images
│   │       └── application.properties
├── database/
│   └── electronic_store_schema.sql # Database schema
├── pom.xml                         # Maven dependencies
└── README.md
```

## 👤 Tài khoản mặc định

### Admin
- **Email**: admin@electronicstore.com
- **Mật khẩu**: password

### Manager
- **Email**: manager@electronicstore.com
- **Mật khẩu**: password

### Customer
- **Email**: customer1@example.com
- **Mật khẩu**: password

## 🌟 Dữ liệu mẫu

Database đã bao gồm:
- 8 danh mục sản phẩm
- 14+ sản phẩm mẫu (Laptop, điện thoại, tablet, phụ kiện...)
- Tài khoản admin và customer
- Đơn hàng và đánh giá mẫu

## 🔧 Customization

### Thêm sản phẩm mới
1. Truy cập admin panel
2. Hoặc thêm trực tiếp vào database thông qua phpMyAdmin

### Thay đổi giao diện
- Chỉnh sửa file Thymeleaf trong `src/main/resources/templates/`
- CSS tùy chỉnh trong các file template hoặc tạo file riêng

### Cấu hình email
Thêm vào `application.properties`:
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🐛 Xử lý sự cố

### Lỗi kết nối database
1. Kiểm tra XAMPP MySQL đã khởi động
2. Kiểm tra tên database và thông tin kết nối
3. Kiểm tra firewall

### Lỗi port 8080 đã được sử dụng
Thêm vào `application.properties`:
```properties
server.port=8081
```

### Lỗi encoding tiếng Việt
Đảm bảo database sử dụng `utf8mb4_unicode_ci`

## 📈 Tối ưu hóa

### Performance
- Sử dụng pagination cho danh sách sản phẩm
- Lazy loading cho relationships
- Database indexing đã được setup

### Security
- Password encoding với BCrypt
- CSRF protection
- SQL injection prevention với JPA

## 🤝 Đóng góp

1. Fork dự án
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📄 License

Dự án này được phân phối dưới [MIT License](LICENSE).

## 📞 Liên hệ

- **Email**: developer@electronicstore.com
- **GitHub**: [https://github.com/your-username/electronic-store](https://github.com/your-username/electronic-store)

---
