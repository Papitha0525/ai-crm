package com.aicrm.backend.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String userId;
    private String message;
    private String sessionId;
}