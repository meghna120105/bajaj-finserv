# Bajaj Finserv Health | Java Qualifier

## About
Spring Boot app that on startup:
1. Calls `generateWebhook` API to get a webhook URL + access token
2. Solves the assigned SQL problem (Question 1 — regNo ends in odd digit)
3. Submits the final SQL query to the webhook URL using JWT auth

## Setup

### Prerequisites
- Java 17+
- Maven 3.6+

### Update your details
In `src/main/java/com/bajaj/service/WebhookService.java`, update:
```java
new WebhookRequest(
    "Your Name",       // your name
    "REGXXXXX",        // your regNo
    "you@email.com"    // your email
)
```

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
java -jar target/bajaj-finserv.jar
```

## Project Structure
```
src/main/java/com/bajaj/
├── BajajApplication.java       # Spring Boot entry point
├── StartupRunner.java          # ApplicationRunner - triggers on startup
├── model/
│   ├── WebhookRequest.java
│   └── WebhookResponse.java
└── service/
    └── WebhookService.java     # Core logic + SQL query
```
