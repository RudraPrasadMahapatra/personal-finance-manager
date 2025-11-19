package com.rudra.finance.tracker.repository;
import com.rudra.finance.tracker.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository {
  
    // 1. Basic CRUD Operations
    int save(Transaction transaction);
    int update(Transaction transaction); 
    int deleteById(int transactionId);
    Transaction findById(int transactionId);
    
    // 2. Dashboard Metrics
    BigDecimal findMonthlyTotalByUserId(int userId, int month, int year);
    
    BigDecimal findTotalByUserId(int userId);
    
    long countByUserId(int userId);
    
    Transaction findLatestByUserId(int userId);

    // 3. View Transactions (List)
    List<Transaction> findAllByUserId(int userId); 

    // 4. Reports & Analytics (Data Retrieval)
    
    List<String> findDistinctCategoriesByUserId(int userId);
    
    List<Object[]> findCategorySpendingByUserId(int userId);

    List<Object[]> sumByCategoryFiltered(int userId,
                                         LocalDate fromDate,
                                         LocalDate toDate,
                                         String category,
                                         BigDecimal minAmt,
                                         BigDecimal maxAmt);

    List<Object[]> sumByDateFiltered(int userId,
                                     LocalDate fromDate,
                                     LocalDate toDate,
                                     String category,
                                     BigDecimal minAmt,
                                     BigDecimal maxAmt);

    BigDecimal sumTotalFiltered(int userId,
                                LocalDate fromDate,
                                LocalDate toDate,
                                String category,
                                BigDecimal minAmt,
                                BigDecimal maxAmt);

}