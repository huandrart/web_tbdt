package com.electronicstore.repository;

import com.electronicstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    List<User> findByIsActiveTrue();
    
    List<User> findByRoleAndIsActiveTrue(String role);
    
    long countByRoleAndIsActiveTrue(String role);

    // Sửa lại role cho đúng với ENUM('USER','ADMIN')
    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.isActive = true")
    List<User> findActiveCustomers();

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findAdminUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER' AND u.isActive = true")
    long countActiveCustomers();
}
