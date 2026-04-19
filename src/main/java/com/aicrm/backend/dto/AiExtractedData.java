package com.aicrm.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiExtractedData {
    private String intent;
    private Map<String, String> data;
    private Map<String, String> filters;
    private List<String> missingFields;
}