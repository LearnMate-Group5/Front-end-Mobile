package com.example.learnmate_frontend.model;
import com.google.gson.annotations.SerializedName;
public class User {
    private int id;
    @SerializedName("fullName")
    private String name;
    private String email;
    @SerializedName("roleId")
    private int roleId; // Thay role (String) bằng roleId (int)
    @SerializedName("avatarUrl")
    private String avatarUrl;
    private String phoneNumber;
    private boolean isActive;
    private String emailVerifiedAt;
    private String lastLoginAt;
    private String createdAt;
    private String updatedAt;

    public User() {
    }

    // Constructor cơ bản cho các trường chính
    public User(int id, String name, String email, int roleId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roleId = roleId;
    }

    // Getters và setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
