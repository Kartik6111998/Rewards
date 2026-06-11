# Customer Rewards API

A Spring Boot REST API that calculates reward points for a retailer's customer loyalty program.

---

## Points Calculation Rules

| Spend in a Single Transaction | Points Earned |
|-------------------------------|---------------|
| $0 – $50                      | 0 points      |
| $51 – $100                    | 1 point per dollar over $50 |
| Over $100                     | 1 point per dollar between $50–$100 **+** 2 points per dollar over $100 |

**Example:** A $120 purchase earns `2×$20 + 1×$50 = 90 points`

---

## Project Structure

```
src/
├── main/java/com/rewards/
│   ├── RewardsApplication.java         # Entry point
│   ├── DataLoader.java                 # Seed data (3 customers, 3 months)
│   ├── controller/
│   │   └── RewardsController.java      # REST endpoints
│   ├── service/
│   │   └── RewardsService.java         # Points calculation logic
│   ├── model/
│   │   ├── Transaction.java            # JPA entity
│   │   └── RewardSummary.java          # Response DTO
│   └── repository/
│       └── TransactionRepository.java  # Spring Data JPA
└── test/java/com/rewards/
    ├── service/RewardsServiceTest.java              # Unit tests (Mockito)
    └── controller/RewardsControllerIntegrationTest.java  # Integration tests
```

---

## API Endpoints

### Get all customer reward summaries
```
GET /api/rewards
```

### Get reward summary for a specific customer
```
GET /api/rewards/{customerId}
```
**Response example:**
```json
{
  "customerId": 1,
  "pointsPerMonth": {
    "2024-10": 90,
    "2024-11": 275,
    "2024-12": 70
  },
  "totalPoints": 435
}
```

### Add a new transaction
```
POST /api/rewards/transactions
Content-Type: application/json

{
  "customerId": 1,
  "amount": 120.00,
  "transactionDate": "2024-12-01"
}
```

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Start the server
```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### H2 Console (dev)
Navigate to `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:rewardsdb`
- Username: `sa`
- Password: *(leave blank)*

### Run tests
```bash
mvn test
```

---

## Sample Data

On startup, `DataLoader` inserts transactions for **3 customers** across the **last 3 months**:

| Customer | Month -2 | Month -1 | Current Month |
|----------|----------|----------|---------------|
| 1        | $120, $45| $75, $200| $110          |
| 2        | $300     | $150     | $95           |
| 3        | $60      | $30, $130| $85           |
