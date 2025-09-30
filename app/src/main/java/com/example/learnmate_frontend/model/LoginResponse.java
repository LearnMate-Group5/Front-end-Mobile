package com.example.learnmate_frontend.model;

public class LoginResponse {
    private String token;
    private int userId;
    private String role; // Thêm trường role

    private int roleId; // Thêm trường roleId
    // Getters và setters
    public String getToken() { return token; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
}
