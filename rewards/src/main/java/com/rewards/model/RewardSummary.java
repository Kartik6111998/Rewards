package com.rewards.model;

import java.util.Map;

/**
 * Data Transfer Object representing the reward points summary for a customer.
 * Contains monthly breakdown and total points earned over the tracked period.
 */
public class RewardSummary {

    private Long customerId;

    /** Points earned per month, keyed by "YYYY-MM" (e.g., "2024-01"). */
    private Map<String, Integer> pointsPerMonth;

    /** Total reward points earned across all months. */
    private int totalPoints;

    public RewardSummary() {}

    public RewardSummary(Long customerId, Map<String, Integer> pointsPerMonth, int totalPoints) {
        this.customerId = customerId;
        this.pointsPerMonth = pointsPerMonth;
        this.totalPoints = totalPoints;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Map<String, Integer> getPointsPerMonth() { return pointsPerMonth; }
    public void setPointsPerMonth(Map<String, Integer> pointsPerMonth) { this.pointsPerMonth = pointsPerMonth; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
