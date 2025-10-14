package com.electronicstore.entity;

public enum UserRole {
    USER("USER", "Người dùng", 1),
    SHIPPER("SHIPPER", "Người giao hàng", 2),
    ADMIN("ADMIN", "Quản trị viên", 3),
    SUPER_ADMIN("SUPER_ADMIN", "Siêu quản trị", 4);
    
    private final String code;
    private final String displayName;
    private final int level;
    
    UserRole(String code, String displayName, int level) {
        this.code = code;
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean canManage(UserRole otherRole) {
        return this.level > otherRole.level;
    }
    
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER;
    }
}
