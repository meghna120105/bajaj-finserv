package com.bajaj.service;

import com.bajaj.model.WebhookRequest;
import com.bajaj.model.WebhookResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GENERATE_URL =
        "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public void execute() {
        System.out.println("=== Bajaj Finserv Qualifier Starting ===");

        // Step 1: Generate Webhook
        WebhookRequest request = new WebhookRequest(
            "Meghna Mishra",
            "ADT23SOCB0604",
            "meghnamihsra885@gmail.com"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);

        WebhookResponse webhookResponse = null;

        try {
            ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                GENERATE_URL,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
            );
            webhookResponse = response.getBody();
        } catch (Exception e) {
            System.err.println("ERROR calling generateWebhook: " + e.getMessage());
            return;
        }

        if (webhookResponse == null || webhookResponse.getWebhook() == null) {
            System.err.println("ERROR: Null or empty webhook response.");
            return;
        }

        System.out.println("Webhook URL     : " + webhookResponse.getWebhook());
        System.out.println("Access Token    : " + webhookResponse.getAccessToken());

        // Step 2: Submit Answer with up to 4 retries
        boolean success = false;
        for (int attempt = 1; attempt <= 4; attempt++) {
            System.out.println("Submission attempt #" + attempt);
            success = submitAnswer(webhookResponse.getWebhook(), webhookResponse.getAccessToken());
            if (success) break;
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }

        if (!success) {
            System.err.println("All submission attempts failed.");
        }
    }

    private boolean submitAnswer(String webhookUrl, String accessToken) {
        // regNo: ADT23SOCB0604 -> ends in 04 (EVEN) -> Question 2
        // Problem: For each employee, count employees in the SAME department who are YOUNGER
        // Younger = DOB is greater (born later)
        // Ordered by EMP_ID DESC
        String finalQuery =
            "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
            "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
            "FROM EMPLOYEE e1 " +
            "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
            "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB " +
            "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
            "ORDER BY e1.EMP_ID DESC";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> result = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            System.out.println("Submission Status : " + result.getStatusCode());
            System.out.println("Submission Body   : " + result.getBody());
            return result.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Submission error: " + e.getMessage());
            return false;
        }
    }
}
