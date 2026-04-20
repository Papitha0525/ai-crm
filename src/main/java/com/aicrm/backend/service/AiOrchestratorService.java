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

        if (!isCrmRelated(input)) {
            return "Sorry, I can only answer CRM-related questions. / மன்னிக்கவும், CRM தொடர்பான கேள்விகள் மட்டுமே பதில் சொல்வேன்.";
        }

        try {

            String systemPrompt = """
You are AI CRM Pro Assistant.
You understand English, Tamil, and Tanglish (Tamil written in English letters like 'leads evlo iruku', 'customer sollu', 'deal paru').
Always reply in the SAME language the user used.
If user writes in Tanglish, reply in Tanglish.
If user writes in Tamil, reply in Tamil.
If user writes in English, reply in English.

Rules:
1. Reply only for CRM topics.
2. CRM means leads, sales, follow-up, customer support, pipeline, deals, contacts, tasks, conversion, retention.
3. Give professional but friendly replies.
4. Maximum 3 short paragraphs.
5. Each paragraph maximum 2 lines.
6. No unnecessary long content.
7. No markdown symbols.
8. No emojis.
9. If outside CRM topic say: Sorry, I can only answer CRM-related questions.
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
            user.put("content", input);

            messages.add(system);
            messages.add(user);

            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            String reply = extractReply(response.getBody());
            return cleanReply(reply);

        } catch (Exception e) {
            return "AI service is temporarily unavailable. Please try again.";
        }
    }

    private boolean isCrmRelated(String text) {

        String q = text.toLowerCase();

        String[] crmWords = {
            "crm", "customer", "lead", "leads", "sales", "pipeline",
            "follow up", "followup", "prospect", "client", "deal", "deals",
            "conversion", "retention", "marketing", "support", "ticket",
            "contact", "contacts", "opportunity", "revenue", "task", "tasks",
            "report", "forecast", "account", "invoice",
            "evlo", "iruku", "sollu", "paru", "panni",
            "epdi", "yaaru", "enna", "pudhu", "paathu",
            "follow", "status", "update", "create", "add",
            "delete", "list", "show", "get", "give",
            "vaaippu", "thodarbu", "varuvai", "velai"
        };

        for (String word : crmWords) {
            if (q.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private String extractReply(Map<String, Object> responseBody) {

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