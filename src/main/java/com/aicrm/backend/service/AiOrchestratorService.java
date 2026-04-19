package com.aicrm.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiOrchestratorService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGroq(String userMessage) {

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please enter a valid CRM question.";
        }

        String input = userMessage.trim();

        // CRM only filter
        if (!isCrmRelated(input)) {
            return "Please ask CRM-related questions only.";
        }

        try {

            String systemPrompt = """
You are AI CRM Pro Assistant.

Rules:
1. Reply only for CRM topics.
2. CRM means leads, sales, follow-up, customer support, pipeline, conversion, retention.
3. Give professional business replies.
4. Maximum 3 short paragraphs.
5. Each paragraph maximum 2 lines.
6. No unnecessary long content.
7. No markdown symbols.
8. No emojis.
9. Clear corporate tone.
10. If outside CRM topic say:
Please ask CRM-related questions only.
""";

            Map<String, Object> body = new HashMap<>();

            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("max_tokens", 120);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("content", systemPrompt);

            Map<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", input);

            messages.add(system);
            messages.add(user);

            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            String reply = extractReply(response.getBody());

            return cleanReply(reply);

        } catch (Exception e) {
            return "AI service is temporarily unavailable.";
        }
    }

    private boolean isCrmRelated(String text) {

        String q = text.toLowerCase();

        String[] crmWords = {
                "crm",
                "customer",
                "lead",
                "sales",
                "pipeline",
                "follow up",
                "followup",
                "prospect",
                "client",
                "deal",
                "conversion",
                "retention",
                "marketing",
                "support",
                "ticket",
                "contact",
                "opportunity",
                "revenue"
        };

        for (String word : crmWords) {
            if (q.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private String extractReply(Map responseBody) {

        if (responseBody == null) {
            return "No response received.";
        }

        Object choicesObj = responseBody.get("choices");

        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return "No response received.";
        }

        Object first = choices.get(0);

        if (!(first instanceof Map<?, ?> firstChoice)) {
            return "No response received.";
        }

        Object msgObj = firstChoice.get("message");

        if (!(msgObj instanceof Map<?, ?> messageMap)) {
            return "No response received.";
        }

        Object content = messageMap.get("content");

        return content == null ? "No response received." : content.toString();
    }

    private String cleanReply(String text) {

        if (text == null || text.isBlank()) {
            return "No response received.";
        }

        String result = text;

        result = result.replace("*", "");
        result = result.replace("#", "");
        result = result.replace("•", "");
        result = result.trim();

        result = result.replace(". ", ".\n\n");

        while (result.contains("\n\n\n")) {
            result = result.replace("\n\n\n", "\n\n");
        }

        return result.trim();
    }
}