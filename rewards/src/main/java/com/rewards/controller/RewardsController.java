package com.rewards.controller;

import com.rewards.model.RewardSummary;
import com.rewards.model.Transaction;
import com.rewards.service.RewardsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful controller exposing endpoints for the customer rewards program.
 *
 * <p>Base path: /api/rewards
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * GET /api/rewards
     * Returns reward point summaries for all customers over the last 3 months.
     *
     * @return list of RewardSummary objects
     */
    @GetMapping
    public ResponseEntity<List<RewardSummary>> getAllRewards() {
        return ResponseEntity.ok(rewardsService.getAllRewardSummaries());
    }

    /**
     * GET /api/rewards/{customerId}
     * Returns the reward summary for a specific customer over the last 3 months.
     *
     * @param customerId the customer's ID
     * @return RewardSummary for the customer
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<RewardSummary> getRewardsByCustomer(@PathVariable Long customerId) {
        RewardSummary summary = rewardsService.getRewardSummary(customerId);
        return ResponseEntity.ok(summary);
    }

    /**
     * POST /api/rewards/transactions
     * Records a new transaction for a customer.
     *
     * @param transaction the transaction details (customerId, amount, transactionDate)
     * @return the saved transaction with its generated ID
     */
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> addTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction saved = rewardsService.saveTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
