package com.rewards;

import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Loads sample transaction data on application startup for demonstration and testing.
 *
 * <p>Covers three customers across three months, including transactions above $100,
 * between $50-$100, and below $50 (no points) to exercise all reward tiers.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final TransactionRepository transactionRepository;

    public DataLoader(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        LocalDate now = LocalDate.now();
        LocalDate month1 = now.minusMonths(2).withDayOfMonth(15);
        LocalDate month2 = now.minusMonths(1).withDayOfMonth(10);
        LocalDate month3 = now.withDayOfMonth(5);

        // Customer 1 — varied spend across all three months
        List<Transaction> customer1 = List.of(
                new Transaction(1L, new BigDecimal("120.00"), month1),  // 90 pts
                new Transaction(1L, new BigDecimal("45.00"),  month1),  //  0 pts
                new Transaction(1L, new BigDecimal("75.00"),  month2),  // 25 pts
                new Transaction(1L, new BigDecimal("200.00"), month2),  // 250 pts
                new Transaction(1L, new BigDecimal("110.00"), month3)   // 70 pts
        );

        // Customer 2 — high spender
        List<Transaction> customer2 = List.of(
                new Transaction(2L, new BigDecimal("300.00"), month1),  // 450 pts
                new Transaction(2L, new BigDecimal("150.00"), month2),  // 150 pts
                new Transaction(2L, new BigDecimal("95.00"),  month3)   //  45 pts
        );

        // Customer 3 — moderate spender with a no-points transaction
        List<Transaction> customer3 = List.of(
                new Transaction(3L, new BigDecimal("60.00"),  month1),  //  10 pts
                new Transaction(3L, new BigDecimal("30.00"),  month2),  //   0 pts
                new Transaction(3L, new BigDecimal("130.00"), month2),  // 110 pts
                new Transaction(3L, new BigDecimal("85.00"),  month3)   //  35 pts
        );

        transactionRepository.saveAll(customer1);
        transactionRepository.saveAll(customer2);
        transactionRepository.saveAll(customer3);

        System.out.println("Sample transaction data loaded for customers 1, 2, and 3.");
    }
}
