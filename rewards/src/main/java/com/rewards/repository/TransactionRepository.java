package com.rewards.repository;

import com.rewards.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for performing CRUD operations on Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a specific customer within a date range.
     *
     * @param customerId      the ID of the customer
     * @param startDate       the start of the date range
     * @param endDate         the end of the date range
     * @return list of matching transactions
     */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Finds all transactions across all customers within a date range.
     * Used by getAllRewardSummaries to avoid N+1 queries.
     *
     * @param startDate the start of the date range
     * @param endDate   the end of the date range
     * @return list of all transactions in the range
     */
    List<Transaction> findAllByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Finds all transactions for a specific customer.
     *
     * @param customerId the ID of the customer
     * @return list of all transactions for the customer
     */
    List<Transaction> findByCustomerId(Long customerId);
}
