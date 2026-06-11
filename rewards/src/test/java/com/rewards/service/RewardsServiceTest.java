package com.rewards.service;

import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.RewardSummary;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RewardsService.
 */
@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    @Test
    @DisplayName("Amount exactly $50 earns 0 points")
    void calculatePoints_exactlyFifty_returnsZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("Amount below $50 earns 0 points")
    void calculatePoints_belowFifty_returnsZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("30.00")));
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("$75 earns 25 points (1 pt per dollar between $50–$100)")
    void calculatePoints_seventyFive_returns25Points() {
        assertEquals(25, rewardsService.calculatePoints(new BigDecimal("75.00")));
    }

    @Test
    @DisplayName("$100 earns exactly 50 points")
    void calculatePoints_exactlyHundred_returnsFiftyPoints() {
        assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("$120 earns 90 points (assignment example)")
    void calculatePoints_oneTwenty_returnsNinetyPoints() {
        assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120.00")));
    }

    @Test
    @DisplayName("$200 earns 250 points")
    void calculatePoints_twoHundred_returns250Points() {
        assertEquals(250, rewardsService.calculatePoints(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("$101 earns 52 points")
    void calculatePoints_justOverHundred_returns52Points() {
        assertEquals(52, rewardsService.calculatePoints(new BigDecimal("101.00")));
    }

    @Test
    @DisplayName("Summary aggregates points correctly per month")
    void getRewardSummary_multipleTransactions_correctMonthlyAndTotal() {
        LocalDate now = LocalDate.now();

        List<Transaction> allTx = List.of(
                new Transaction(1L, new BigDecimal("120.00"), now.minusMonths(2).withDayOfMonth(1)),
                new Transaction(1L, new BigDecimal("75.00"),  now.minusMonths(1).withDayOfMonth(1)),
                new Transaction(1L, new BigDecimal("40.00"),  now.withDayOfMonth(1))
        );

        when(transactionRepository.findByCustomerId(1L)).thenReturn(allTx);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(allTx);

        RewardSummary summary = rewardsService.getRewardSummary(1L);

        assertEquals(1L, summary.getCustomerId());
        assertEquals(115, summary.getTotalPoints()); // 90 + 25 + 0
    }

    @Test
    @DisplayName("Summary returns 0 total when no transactions qualify for points")
    void getRewardSummary_noQualifyingTransactions_returnsZeroTotal() {
        List<Transaction> lowTx = List.of(
                new Transaction(1L, new BigDecimal("30.00"), LocalDate.now()),
                new Transaction(1L, new BigDecimal("10.00"), LocalDate.now())
        );

        when(transactionRepository.findByCustomerId(1L)).thenReturn(lowTx);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(lowTx);

        RewardSummary summary = rewardsService.getRewardSummary(1L);
        assertEquals(0, summary.getTotalPoints());
    }

    @Test
    @DisplayName("Non-existent customer throws CustomerNotFoundException")
    void getRewardSummary_nonExistentCustomer_throwsNotFoundException() {
        when(transactionRepository.findByCustomerId(999L)).thenReturn(List.of());

        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getRewardSummary(999L));
    }

    @Test
    @DisplayName("CustomerNotFoundException message contains the customer ID")
    void getRewardSummary_nonExistentCustomer_exceptionMessageContainsId() {
        when(transactionRepository.findByCustomerId(42L)).thenReturn(List.of());

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getRewardSummary(42L));

        assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    @DisplayName("saveTransaction delegates to repository and returns saved entity")
    void saveTransaction_validTransaction_returnsSaved() {
        Transaction tx = new Transaction(1L, new BigDecimal("150.00"), LocalDate.now());
        when(transactionRepository.save(tx)).thenReturn(tx);

        Transaction result = rewardsService.saveTransaction(tx);
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getAmount());
    }
}
