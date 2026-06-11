package com.rewards.service;

import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.RewardSummary;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for calculating customer reward points.
 *
 * <p>Points rules:
 * <ul>
 *   <li>2 points for every dollar spent over $100 in a single transaction</li>
 *   <li>1 point for every dollar spent between $50 and $100 in a single transaction</li>
 *   <li>No points for amounts $50 or under</li>
 * </ul>
 *
 * <p>Example: $120 purchase = 2×$20 + 1×$50 = 90 points
 */
@Service
public class RewardsService {

    private static final BigDecimal TIER_TWO_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal TIER_ONE_THRESHOLD = new BigDecimal("50");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;

    public RewardsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates the reward points earned for a single transaction amount.
     *
     * @param amount the dollar amount of the transaction
     * @return the number of reward points earned
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount.compareTo(TIER_ONE_THRESHOLD) <= 0) {
            return 0;
        }

        int points = 0;

        if (amount.compareTo(TIER_TWO_THRESHOLD) > 0) {
            // 2 points per dollar above $100
            points += amount.subtract(TIER_TWO_THRESHOLD).intValue() * 2;
            // 1 point per dollar between $50 and $100
            points += TIER_TWO_THRESHOLD.subtract(TIER_ONE_THRESHOLD).intValue();
        } else {
            // 1 point per dollar between $50 and the amount
            points += amount.subtract(TIER_ONE_THRESHOLD).intValue();
        }

        return points;
    }

    /**
     * Retrieves the reward points summary for a customer over the last three months.
     * Throws CustomerNotFoundException if the customer has no transaction history.
     *
     * @param customerId the ID of the customer
     * @return a RewardSummary with monthly and total point breakdowns
     * @throws CustomerNotFoundException if no transactions found for the customer
     */
    @Transactional(readOnly = true)
    public RewardSummary getRewardSummary(Long customerId) {
        // Validate customer exists before building summary
        List<Transaction> allCustomerTx = transactionRepository.findByCustomerId(customerId);
        if (allCustomerTx.isEmpty()) {
            throw new CustomerNotFoundException("No transactions found for customer ID: " + customerId);
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3).withDayOfMonth(1);

        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndTransactionDateBetween(customerId, startDate, endDate);

        Map<String, Integer> pointsPerMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().format(MONTH_FORMATTER),
                        LinkedHashMap::new,
                        Collectors.summingInt(t -> calculatePoints(t.getAmount()))
                ));

        int totalPoints = pointsPerMonth.values().stream().mapToInt(Integer::intValue).sum();

        return new RewardSummary(customerId, pointsPerMonth, totalPoints);
    }

    /**
     * Retrieves reward summaries for all distinct customers in the system.
     *
     * @return list of RewardSummary for every customer
     */
    @Transactional(readOnly = true)
    public List<RewardSummary> getAllRewardSummaries() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3).withDayOfMonth(1);

        // Single query for all transactions
        List<Transaction> allTransactions = transactionRepository
                .findAllByTransactionDateBetween(startDate, endDate);

        // Group by customerId → month → sum points
        Map<Long, Map<String, Integer>> byCustomer = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCustomerId,
                        Collectors.groupingBy(
                                t -> t.getTransactionDate().format(MONTH_FORMATTER),
                                LinkedHashMap::new,
                                Collectors.summingInt(t -> calculatePoints(t.getAmount()))
                        )
                ));

        return byCustomer.entrySet().stream()
                .map(e -> {
                    int total = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
                    return new RewardSummary(e.getKey(), e.getValue(), total);
                })
                .collect(Collectors.toList());
    }

    /**
     * Saves a new transaction to the repository.
     *
     * @param transaction the transaction to persist
     * @return the saved transaction with generated ID
     */
    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
