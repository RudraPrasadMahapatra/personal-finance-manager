package com.rudra.finance.tracker.repository;

import com.rudra.finance.tracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper Implementation
    // This maps columns from the 'users' table to fields in the User POJO.
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPfpUrl(rs.getString("pfp_url"));
            
            // Convert java.sql.Timestamp to modern java.time.LocalDateTime
            if (rs.getTimestamp("created_at") != null) {
                user.setCreatedAt(rs.getTimestamp("created_at").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
            }
            return user;
        }
    };

    //Core Database Methods

    @Override
    public int save(User user) {
        // NOTE: We assume 'password' is already BCrypt hashed by the UserService
        String sql = "INSERT INTO users (full_name, email, password, pfp_url) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getFullName(), user.getEmail(), user.getPassword(), user.getPfpUrl());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            // This is the correct exception to catch when no row is found
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findById(int userId) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByFullName(String fullName) {
        try {
            String sql = "SELECT * FROM users WHERE full_name = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, fullName);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}