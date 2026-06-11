package com.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RewardsController.
 * Spins up the full Spring context and tests HTTP endpoints end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/rewards returns 200 and list of summaries")
    void getAllRewards_withData_returns200AndSummaries() throws Exception {
        transactionRepository.save(new Transaction(1L, new BigDecimal("120.00"), LocalDate.now().minusMonths(1)));
        transactionRepository.save(new Transaction(2L, new BigDecimal("75.00"),  LocalDate.now().minusMonths(1)));

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].customerId", containsInAnyOrder(1, 2)));
    }

    @Test
    @DisplayName("GET /api/rewards returns 200 and empty list when no transactions")
    void getAllRewards_noData_returns200AndEmptyList() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} returns correct total points")
    void getRewardsByCustomer_validCustomer_returnsCorrectPoints() throws Exception {
        transactionRepository.save(new Transaction(1L, new BigDecimal("120.00"), LocalDate.now().minusMonths(1)));

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.totalPoints").value(90));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} returns 404 for non-existent customer")
    void getRewardsByCustomer_nonExistentCustomer_returns404() throws Exception {
        mockMvc.perform(get("/api/rewards/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} aggregates multiple months correctly")
    void getRewardsByCustomer_multipleMonths_correctTotal() throws Exception {
        LocalDate now = LocalDate.now();
        transactionRepository.save(new Transaction(10L, new BigDecimal("120.00"), now.minusMonths(2).withDayOfMonth(1))); // 90 pts
        transactionRepository.save(new Transaction(10L, new BigDecimal("200.00"), now.minusMonths(1).withDayOfMonth(1))); // 250 pts
        transactionRepository.save(new Transaction(10L, new BigDecimal("50.00"),  now.withDayOfMonth(1)));               //   0 pts

        mockMvc.perform(get("/api/rewards/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(340));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} returns 400 for invalid path variable")
    void getRewardsByCustomer_invalidPathVariable_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/rewards/transactions saves transaction and returns 201")
    void addTransaction_validPayload_returns201() throws Exception {
        Transaction tx = new Transaction(5L, new BigDecimal("150.00"), LocalDate.now());

        mockMvc.perform(post("/api/rewards/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/rewards/transactions returns 400 for negative amount")
    void addTransaction_negativeAmount_returns400WithErrorBody() throws Exception {
        Transaction tx = new Transaction(5L, new BigDecimal("-50.00"), LocalDate.now());

        mockMvc.perform(post("/api/rewards/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tx)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/rewards/transactions returns 400 when amount is missing")
    void addTransaction_missingAmount_returns400() throws Exception {
        String payload = "{\"customerId\": 1, \"transactionDate\": \"" + LocalDate.now() + "\"}";

        mockMvc.perform(post("/api/rewards/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rewards/transactions returns 400 when customerId is missing")
    void addTransaction_missingCustomerId_returns400() throws Exception {
        String payload = "{\"amount\": 100.00, \"transactionDate\": \"" + LocalDate.now() + "\"}";

        mockMvc.perform(post("/api/rewards/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
