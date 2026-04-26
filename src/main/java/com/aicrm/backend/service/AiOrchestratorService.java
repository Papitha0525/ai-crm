package com.aicrm.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiOrchestratorService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGroq(String userMessage) {

        // DEBUG
        System.out.println("=== GROQ DEBUG ===");
        System.out.println("KEY: [" + apiKey + "]");
        System.out.println("URL: [" + apiUrl + "]");
        System.out.println("MODEL: [" + model + "]");
        System.out.println("MSG: [" + userMessage + "]");
        System.out.println("==================");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please enter a valid question.";
        }

        try {
            String systemPrompt = """
You are AI CRM Pro Assistant.
Answer all CRM related questions professionally.
CRM means leads, sales, follow-up, customer, pipeline, deals.
Give clear short answers. Maximum 3 paragraphs.
No markdown symbols. No emojis. Plain text only.
""";

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.3);
            body.put("max_tokens", 200);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("content", systemPrompt);

            Map<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", userMessage.trim());

            messages.add(system);
            messages.add(user);

            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            System.out.println("Calling Groq API...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            System.out.println("Response status: " + response.getStatusCode());

            String reply = extractReply(response.getBody());
            return cleanReply(reply);

        } catch (Exception e) {
            System.out.println("=== GROQ ERROR ===");
            System.out.println("Error: " + e.getMessage());
            System.out.println("==================");
            e.printStackTrace();
            return "AI Error: " + e.getMessage();
        }
    }

    private String extractReply(Map responseBody) {

        if (responseBody == null) return "No response received.";

        Object choicesObj = responseBody.get("choices");

        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty())
            return "No response received.";

        Object first = choices.get(0);

        if (!(first instanceof Map<?, ?> firstChoice))
            return "No response received.";

        Object msgObj = firstChoice.get("message");

        if (!(msgObj instanceof Map<?, ?> messageMap))
            return "No response received.";

        Object content = messageMap.get("content");

        return content == null ? "No response received." : content.toString();
    }

    private String cleanReply(String text) {

        if (text == null || text.isBlank()) return "No response received.";

        String result = text;
        result = result.replace("**", "");
        result = result.replace("*", "");
        result = result.replace("#", "");
        result = result.replace("•", "-");

        while (result.contains("\n\n\n")) {
            result = result.replace("\n\n\n", "\n\n");
        }

        return result.trim();
    }
}