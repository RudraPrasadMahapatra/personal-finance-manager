package com.rudra.finance.tracker.model;

import java.time.LocalDate;
import java.math.BigDecimal;

// This is a Plain Old Java Object (POJO) for mapping JDBC database rows 
public class Transaction {

    private Integer transactionId; 

    private String title;
    // Using BigDecimal for precise financial amounts
    private BigDecimal amount; 
    private String category;
    
    // Using modern Java time API (LocalDate is sufficient for dates)
    private LocalDate date; 

    private String description;
    
    private Integer userId; 
    
    private User user; 

    // --- Constructors ---
    public Transaction() {}

    // --- Getters and Setters (REQUIRED FOR JDBC ROW MAPPER) ---

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // Setter/Getter for convenience (used when form data is received)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }
    
    // Safe toString (no recursion)
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                '}';
    }
}