package com.rudra.finance.tracker.repository;

import com.rudra.finance.tracker.model.User;
import java.util.Optional;

public interface UserRepository {
    
    // Saves a user to the database (used for registration)
    int save(User user); 
    
    // Finds a user by email (used by login/security)
    Optional<User> findByEmail(String email);
    
    // Finds a user by their unique ID (used by transaction lookup)
    Optional<User> findById(int userId);
    
    // Optional: finds a user by their full name
    Optional<User> findByFullName(String fullName);
}