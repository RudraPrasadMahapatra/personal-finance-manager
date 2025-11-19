package com.rudra.finance.tracker.repository;

import com.rudra.finance.tracker.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TransactionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- RowMapper Implementation ---
    // Maps columns from the 'transactions' table to fields in the Transaction POJO.
    private final RowMapper<Transaction> transactionRowMapper = new RowMapper<Transaction>() {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transaction t = new Transaction();
            t.setTransactionId(rs.getInt("transaction_id"));
            t.setUserId(rs.getInt("user_id"));
            t.setTitle(rs.getString("title"));
            t.setAmount(rs.getBigDecimal("amount"));
            t.setCategory(rs.getString("category"));
            t.setDescription(rs.getString("description"));
            
            // Convert java.sql.Date to modern java.time.LocalDate
            if (rs.getDate("transaction_date") != null) {
                t.setDate(rs.getDate("transaction_date").toLocalDate());
            }
            return t;
        }
    };

    // --- 1. Basic CRUD Operations ---

    @Override
    public int save(Transaction transaction) {
        final String sql = "INSERT INTO transactions (user_id, title, amount, category, description, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        // Use KeyHolder to retrieve the auto-generated transaction_id
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, transaction.getUserId());
            ps.setString(2, transaction.getTitle());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getCategory());
            ps.setString(5, transaction.getDescription());
            ps.setObject(6, transaction.getDate()); // LocalDate handling
            return ps;
        }, keyHolder);

        // Set the generated ID back on the Transaction object
        if (keyHolder.getKey() != null) {
            transaction.setTransactionId(keyHolder.getKey().intValue());
            return 1;
        }
        return 0;
    }

    @Override
    public int update(Transaction transaction) {
        final String sql = "UPDATE transactions SET title=?, amount=?, category=?, description=?, transaction_date=? WHERE transaction_id=? AND user_id=?";
        return jdbcTemplate.update(sql, 
            transaction.getTitle(), 
            transaction.getAmount(), 
            transaction.getCategory(), 
            transaction.getDescription(), 
            transaction.getDate(), 
            transaction.getTransactionId(),
            transaction.getUserId()
        );
    }

    @Override
    public int deleteById(int transactionId) {
        final String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        return jdbcTemplate.update(sql, transactionId);
    }
    
    @Override
    public Transaction findById(int transactionId) {
        try {
            final String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
            return jdbcTemplate.queryForObject(sql, transactionRowMapper, transactionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // --- 2. Dashboard Metrics ---

    @Override
    public BigDecimal findMonthlyTotalByUserId(int userId, int month, int year) {
        final String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                           "WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, month, year);
    }
    
    @Override
    public BigDecimal findTotalByUserId(int userId) {
        final String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
    }

    @Override
    public long countByUserId(int userId) {
        final String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        // queryForObject returns a Long, but we must handle possible null if table is empty
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }
    
    @Override
    public Transaction findLatestByUserId(int userId) {
        try {
            final String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_id DESC LIMIT 1";
            return jdbcTemplate.queryForObject(sql, transactionRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // --- 3. View Transactions (List) ---

    @Override
    public List<Transaction> findAllByUserId(int userId) {
        final String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC, transaction_id DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, userId);
    }

    // --- 4. Reports & Analytics (Data Retrieval) ---
    
    @Override
    public List<String> findDistinctCategoriesByUserId(int userId) {
        final String sql = "SELECT DISTINCT category FROM transactions WHERE user_id = ? ORDER BY category ASC";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }
    
    @Override
    public List<Object[]> findCategorySpendingByUserId(int userId) {
        final String sql = "SELECT category, SUM(amount) FROM transactions WHERE user_id = ? GROUP BY category ORDER BY SUM(amount) DESC";
        // Uses a generalized RowMapper for aggregated results
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { 
            rs.getString(1), 
            rs.getBigDecimal(2) 
        }, userId);
    }

    // --- Complex Filtering Methods (Simplified Dynamic SQL) ---

    // IMPORTANT: Implementing dynamic/optional WHERE clauses in JDBC is complex. 
    // This helper method simplifies the creation of the SQL query and argument list.
    private String buildFilterClauses(LocalDate fromDate, LocalDate toDate, String category, BigDecimal minAmt, BigDecimal maxAmt, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        
        if (fromDate != null) {
            sb.append(" AND transaction_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sb.append(" AND transaction_date <= ?");
            params.add(toDate);
        }
        if (category != null && !category.isEmpty()) {
            sb.append(" AND category = ?");
            params.add(category);
        }
        if (minAmt != null) {
            sb.append(" AND amount >= ?");
            params.add(minAmt);
        }
        if (maxAmt != null) {
            sb.append(" AND amount <= ?");
            params.add(maxAmt);
        }
        return sb.toString();
    }


    @Override
    public List<Object[]> sumByCategoryFiltered(int userId, LocalDate fromDate, LocalDate toDate, String category, BigDecimal minAmt, BigDecimal maxAmt) {
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        String filterSql = buildFilterClauses(fromDate, toDate, category, minAmt, maxAmt, params);
        
        final String sql = "SELECT category, SUM(amount) FROM transactions " +
                           "WHERE user_id = ?" + filterSql +
                           " GROUP BY category ORDER BY SUM(amount) DESC";

        // Converts result set (category, sum(amount)) to Object[]
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { 
            rs.getString(1), 
            rs.getBigDecimal(2) 
        }, params.toArray());
    }

    @Override
    public List<Object[]> sumByDateFiltered(int userId, LocalDate fromDate, LocalDate toDate, String category, BigDecimal minAmt, BigDecimal maxAmt) {
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        String filterSql = buildFilterClauses(fromDate, toDate, category, minAmt, maxAmt, params);
        
        final String sql = "SELECT transaction_date, SUM(amount) FROM transactions " +
                           "WHERE user_id = ?" + filterSql +
                           " GROUP BY transaction_date ORDER BY transaction_date ASC";

        // Converts result set (date, sum(amount)) to Object[]
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { 
            rs.getDate(1).toLocalDate(), 
            rs.getBigDecimal(2) 
        }, params.toArray());
    }

    @Override
    public BigDecimal sumTotalFiltered(int userId, LocalDate fromDate, LocalDate toDate, String category, BigDecimal minAmt, BigDecimal maxAmt) {
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        String filterSql = buildFilterClauses(fromDate, toDate, category, minAmt, maxAmt, params);
        
        final String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                           "WHERE user_id = ?" + filterSql;

        return jdbcTemplate.queryForObject(sql, BigDecimal.class, params.toArray());
    }
}