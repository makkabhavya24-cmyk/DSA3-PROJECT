package com.logistics.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String fullName;
    private String role;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message, Long userId, String username, String fullName, String role) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public static AuthResponse fail(String message) {
        return new AuthResponse(false, message, null, null, null, null);
    }

    public static AuthResponse ok(Long userId, String username, String fullName, String role) {
        return new AuthResponse(true, "Login successful", userId, username, fullName, role);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
