package com.aicrm.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC DETAILS
    // ===============================
    private String name;

    private String phone;

    private String email;

    private String city;

    // ===============================
    // CRM DETAILS
    // ===============================
    private String source;     
    // CHAT / MANUAL / WEBSITE / REFERRAL

    private String status;     
    // NEW / CONTACTED / QUALIFIED / LOST

    private String requirement;

    private String followUp;   
    // Date / note

    private String dealStatus; 
    // WON / LOST / PENDING

    // ===============================
    // LEAD ASSIGNMENT
    // ===============================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    // ===============================
    // CREATED DATE
    // ===============================
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        createdAt = LocalDateTime.now();

        if (status == null || status.isBlank()) {
            status = "NEW";
        }

        if (dealStatus == null || dealStatus.isBlank()) {
            dealStatus = "PENDING";
        }

        if (source == null || source.isBlank()) {
            source = "MANUAL";
        }
    }
}