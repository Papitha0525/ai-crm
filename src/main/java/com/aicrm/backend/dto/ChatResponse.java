package com.aicrm.backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String reply;
    private String action;  // ASK_MISSING_FIELD, LEAD_CREATED, TASK_CREATED
    private Object data;    // optional - created object
}