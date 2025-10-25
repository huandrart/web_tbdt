package com.electronicstore.service;

import com.electronicstore.entity.User;
import com.electronicstore.entity.UserRole;
import com.electronicstore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String ROLE_PREFIX = "ROLE_";
    private static final UserRole DEFAULT_ROLE = UserRole.USER;
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.*");

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.email.reset.expiration}")
    private int resetExpirationHours;

    // Spring Security UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserRole role = (user.getRole() == null) ? DEFAULT_ROLE : user.getRole();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getIsActive()), // enabled
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                getAuthorities(role.getCode())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singleton(new SimpleGrantedAuthority(ROLE_PREFIX + role));
    }

    // Đăng ký user (từ form)
    public User createUser(User user) {
        log.debug("Creating user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            log.debug("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Mặc định role = USER nếu null
        if (user.getRole() == null) {
            user.setRole(DEFAULT_ROLE);
        }

        // Mã hoá mật khẩu nếu chưa mã hoá
        if (user.getPassword() != null && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Tạo verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));
        user.setEmailVerified(false);
        user.setIsActive(false); // Tạm thời khóa tài khoản cho đến khi verify email

        User saved = userRepository.save(user);
        
        // Gửi email verification
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationToken);
            log.debug("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            // Không throw exception để không làm fail registration
        }
        
        log.debug("User saved with ID: {}", saved.getId());
        return saved;
    }

    // API Registration method
    public User registerUser(String username, String email, String password, String fullName) {
        log.debug("API registerUser called with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.debug("Email already exists: {}", email);
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(DEFAULT_ROLE);
        user.setIsActive(true);

        User saved = userRepository.save(user);
        log.debug("API User saved with ID: {}", saved.getId());
        return saved;
    }

    // Tìm user theo email cho authentication
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // User management methods
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public List<User> findActiveCustomers() {
        // tuỳ định nghĩa "customer" = role USER
        return userRepository.findByRoleAndIsActiveTrue("USER");
    }

    public List<User> findAdminUsers() {
        return userRepository.findByRoleAndIsActiveTrue("ADMIN");
    }

    public Optional<User> findByIdOptional(Long id) {
        return userRepository.findById(id);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // Search users with filters
    public Page<User> searchUsers(String search, String role, Boolean isActive, Pageable pageable) {
        // If no filters, return all users
        if ((search == null || search.trim().isEmpty()) && 
            (role == null || role.trim().isEmpty()) && 
            isActive == null) {
            return userRepository.findAll(pageable);
        }
        
        // For now, implement basic filtering
        // TODO: Implement proper search with JPA Specifications
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        // encode nếu chưa bcrypt
        if (user.getPassword() != null && !user.getPassword().isBlank()
                && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // chuẩn hoá role
        if (user.getRole() == null) {
            user.setRole(DEFAULT_ROLE);
        } else {
            // Role đã được set đúng từ trước
        }
        return userRepository.save(user);
    }

    public User register(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        user.setRole(DEFAULT_ROLE);
        user.setIsActive(true);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID không được null khi cập nhật");
        }

        return userRepository.findById(user.getId()).map(existing -> {
            // chỉ cập nhật các field an toàn
            existing.setFullName(user.getFullName());
            existing.setPhone(user.getPhone());
            existing.setAddress(user.getAddress());

            // chỉ cập nhật password nếu có
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                if (!BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
                    existing.setPassword(passwordEncoder.encode(user.getPassword()));
                } else {
                    existing.setPassword(user.getPassword());
                }
            }

            // cho phép đổi trạng thái active nếu truyền vào (tuỳ policy)
            if (user.getIsActive() != null) {
                existing.setIsActive(user.getIsActive());
            }

            // đổi role nếu cần và hợp lệ
            if (user.getRole() != null) {
                existing.setRole(user.getRole());
            }

            return userRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    public void softDeleteById(Long id) {
        // Implementation for soft delete
    }
    
    // Email verification methods
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Kiểm tra token có hết hạn không
        if (user.getVerificationTokenExpires() == null || 
            user.getVerificationTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Verify email và kích hoạt tài khoản
        user.setEmailVerified(true);
        user.setIsActive(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpires(null);
        
        userRepository.save(user);
        log.debug("Email verified for user: {}", user.getEmail());
        return true;
    }
    
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Email không tồn tại");
        }
        
        User user = userOpt.get();
        
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email đã được xác thực");
        }
        
        // Tạo token mới
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));
        
        userRepository.save(user);
        
        // Gửi email verification
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationToken);
        log.debug("Verification email resent to: {}", user.getEmail());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public long countActiveCustomers() {
        return userRepository.countByRoleAndIsActiveTrue("USER");
    }

    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    public long count() {
        return userRepository.count();
    }

    public long countByIsActive(boolean isActive) {
        return userRepository.countByIsActive(isActive);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    // Password reset methods
    public void generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
        }
        
        User user = userOpt.get();
        
        // Kiểm tra tài khoản có bị khóa không
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }
        
        // Tạo reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpires(LocalDateTime.now().plusHours(resetExpirationHours));
        
        userRepository.save(user);
        
        // Gửi email reset password
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
            log.debug("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // Xóa token nếu gửi email thất bại
            user.setResetToken(null);
            user.setResetTokenExpires(null);
            userRepository.save(user);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        }
    }
    
    public boolean validateResetToken(String token) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Kiểm tra token có hết hạn không
        if (user.getResetTokenExpires() == null || 
            user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            // Xóa token hết hạn
            user.setResetToken(null);
            user.setResetTokenExpires(null);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }
    
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        
        User user = userOpt.get();
        
        // Kiểm tra token có hết hạn không
        if (user.getResetTokenExpires() == null || 
            user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            // Xóa token hết hạn
            user.setResetToken(null);
            user.setResetTokenExpires(null);
            userRepository.save(user);
            throw new IllegalArgumentException("Token đã hết hạn");
        }
        
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.debug("Password reset successfully for user: {}", user.getEmail());
    }
}
