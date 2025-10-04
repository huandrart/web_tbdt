package com.electronicstore.service;

import com.electronicstore.entity.User;
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

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_ROLE = "USER"; // khớp ENUM('USER','ADMIN')
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.*");

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Spring Security UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = (user.getRole() == null || user.getRole().isBlank()) ? DEFAULT_ROLE : user.getRole().toUpperCase();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getIsActive()), // enabled
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                getAuthorities(role)
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

        // Mặc định role = USER nếu null/rỗng
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(DEFAULT_ROLE);
        } else {
            user.setRole(user.getRole().toUpperCase());
        }

        // Mã hoá mật khẩu nếu chưa mã hoá
        if (user.getPassword() != null && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setIsActive(true);
        User saved = userRepository.save(user);
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

    // Có thể mở rộng filter thật sự qua Spec/Query Methods
    public Page<User> searchUsers(String search, String role, Boolean isActive, Pageable pageable) {
        // For now, just return all users with pagination
        // TODO: Implement proper search logic
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
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(DEFAULT_ROLE);
        } else {
            user.setRole(user.getRole().toUpperCase());
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
            if (user.getRole() != null && !user.getRole().isBlank()) {
                existing.setRole(user.getRole().toUpperCase());
            }

            return userRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    public void softDeleteById(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setIsActive(false);
            userRepository.save(u);
        });
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
}
