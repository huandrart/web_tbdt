# 🛒 Electronic Store Management System

A comprehensive Spring Boot application for managing an electronic store with full-featured e-commerce capabilities.

## ✨ Features

### 🏪 **Core Features**
- **User Management**: Registration, authentication, profile management
- **Product Catalog**: Categories, products with images, search & filtering
- **Shopping Cart**: Add/remove items, quantity management
- **Order Processing**: Complete order lifecycle management
- **Payment Integration**: Multiple payment methods support
- **Admin Dashboard**: Comprehensive management interface
- **Shipper Management**: Order delivery and tracking system

### 🔐 **Security & Authentication**
- **Role-based Access Control**: USER, ADMIN, SUPER_ADMIN, SHIPPER
- **Password Encryption**: BCrypt security
- **CSRF Protection**: Cross-site request forgery prevention
- **Session Management**: Secure user sessions

### 📱 **User Interface**
- **Responsive Design**: Mobile-first approach
- **Modern UI**: Bootstrap 5.3 with custom styling
- **Interactive Elements**: AJAX-powered features
- **Multi-step Forms**: User-friendly form flows

## 🛠️ **Technologies Used**

### **Backend**
- **Spring Boot 3.1.0** - Main framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework
- **MySQL 8.0** - Database
- **Maven** - Dependency management

### **Frontend**
- **Thymeleaf** - Template engine
- **Bootstrap 5.3** - CSS framework
- **jQuery** - JavaScript library
- **Font Awesome** - Icons
- **Google Fonts** - Typography

### **Development Tools**
- **Spring Boot DevTools** - Hot reload
- **H2 Database** - Development database
- **Maven Wrapper** - Consistent builds

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### **Installation**

1. **Clone the repository**
   ```bash
   git clone 
   cd electronic-store
   ```

2. **Database Setup**
   ```sql
   CREATE DATABASE electronic_store;
   ```
   - Run migration scripts in `database/` folder
   - Update database credentials in `application.properties`

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
   - **Homepage**: http://localhost:8080
   - **Admin Panel**: http://localhost:8080/admin
   - **API Docs**: http://localhost:8080/swagger-ui.html

## 📊 **Default Accounts**

### **Admin Account**
- **Email**: admin1@electronicstore.com
- **Password**: admin1
- **Role**: ADMIN

### **Test User Account**
- **Email**: user@test.com
- **Password**: user123
- **Role**: USER

## 🗂️ **Project Structure**

```
electronic-store/
├── src/main/java/com/electronicstore/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST & MVC controllers
│   ├── entity/          # JPA entities
│   ├── repository/      # Data repositories
│   ├── service/         # Business logic
│   ├── dto/            # Data transfer objects
│   └── util/           # Utility classes
├── src/main/resources/
│   ├── static/         # Static assets (CSS, JS, images)
│   ├── templates/      # Thymeleaf templates
│   └── application.properties
├── database/           # Database migration scripts
├── uploads/           # User uploaded files
└── logs/             # Application logs
```

## 🔧 **Configuration**

### **Environment Variables**
```bash
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# File Upload
UPLOAD_DIR=/path/to/uploads

# Email (Optional)
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_password
```

### **Production Deployment**
1. Set `spring.profiles.active=prod`
2. Configure production database
3. Set up file storage
4. Configure email settings
5. Set up reverse proxy (Nginx/Apache)

## 📈 **API Endpoints**

### **Public Endpoints**
- `GET /` - Homepage
- `GET /products` - Product catalog
- `GET /about` - About page
- `GET /contact` - Contact page
- `POST /contact` - Submit contact form

### **Authentication**
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `GET /forgot-password` - Password reset
- `POST /forgot-password` - Send reset email

### **User Features**
- `GET /profile` - User profile
- `GET /cart` - Shopping cart
- `POST /cart/add` - Add to cart
- `GET /orders` - User orders
- `POST /orders/place` - Place order

### **Admin Features**
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/products` - Product management
- `GET /admin/orders` - Order management
- `GET /admin/users` - User management

## 🧪 **Testing**

### **Run Tests**
```bash
mvn test
```

### **Test Coverage**
```bash
mvn jacoco:report
```

## 📝 **Database Schema**

### **Core Tables**
- `users` - User accounts and profiles
- `categories` - Product categories
- `products` - Product information
- `orders` - Order records
- `order_items` - Order line items
- `carts` - Shopping cart items
- `reviews` - Product reviews

### **Migration Scripts**
- `V1__create_users_table.sql`
- `V2__create_categories_table.sql`
- `V3__create_products_table.sql`
- And more...

## 🔒 **Security Features**

- **Password Hashing**: BCrypt with salt
- **CSRF Protection**: Token-based validation
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Input sanitization
- **Session Security**: Secure session management

## 📱 **Responsive Design**

- **Mobile First**: Optimized for mobile devices
- **Tablet Support**: Responsive breakpoints
- **Desktop Enhanced**: Full feature set on desktop
- **Touch Friendly**: Touch-optimized interactions

## 🚀 **Performance Optimizations**

- **Database Indexing**: Optimized queries
- **Lazy Loading**: Efficient data loading
- **Caching**: Spring Cache integration
- **Image Optimization**: Compressed images
- **CDN Ready**: Static asset optimization

## 🤝 **Contributing**

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### **Development Guidelines**
- Follow Java naming conventions
- Write unit tests for new features
- Update documentation
- Follow the existing code style

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 **Authors**

- **Huân Nguyễn** - *Initial work* - [YourGitHub](https://github.com/huandrart)

## 🙏 **Acknowledgments**

- Spring Boot team for the amazing framework
- Bootstrap team for the UI components
- MySQL team for the database
- All contributors and testers

## 📞 **Support**

- **Email**: huandkdt@gmail.com
- **Issues**: [GitHub Issues](https://github.com/huandrart/tmdt)
- **Documentation**: [Wiki]

---

**Made with ❤️ for the electronic store community**