package com.rudra.finance.tracker.model;

import java.time.LocalDateTime;

// This is a Plain Java Object (POJO) used for mapping database rows (JDBC)
public class User {

    private Integer userId; 
    
    private String fullName; 
    private String email;
    private String password;
    private String pfpUrl;
    
    // Using Java time API
    private LocalDateTime createdAt; 


    // --- Constructors ---
    public User() {}

    // --- Getters and Setters (REQUIRED FOR JDBC ROW MAPPER) ---
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPfpUrl() {
        return pfpUrl;
    }

    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Custom toString is still good practice
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", pfpUrl='" + pfpUrl + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}