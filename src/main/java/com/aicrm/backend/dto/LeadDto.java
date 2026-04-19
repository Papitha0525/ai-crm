package com.aicrm.backend.dto;

import lombok.Data;

@Data
public class LeadDto {
    private String name;
    private String phone;
    private String email;
    private String city;
    private String source;
    private String status;
    private String requirement;
    private Long assignedToId;
    private String followUp;
    private String dealStatus;
}