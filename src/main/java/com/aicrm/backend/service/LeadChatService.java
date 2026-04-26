package com.aicrm.backend.service;

import com.aicrm.backend.dto.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class LeadChatService {

    public ChatResponse createLead(String msg) {
        return new ChatResponse("Lead created", "OK", null);
    }

    public ChatResponse showLeads(String role, String userId) {
        return new ChatResponse("Showing leads", "OK", null);
    }

    public ChatResponse deleteLead(String role, String msg) {
        return new ChatResponse("Lead deleted", "OK", null);
    }

    public ChatResponse updatePhone(String msg) {
        return new ChatResponse("Phone updated", "OK", null);
    }

    public ChatResponse markWon(String msg) {
        return new ChatResponse("Marked WON", "OK", null);
    }

    public ChatResponse markLost(String msg) {
        return new ChatResponse("Marked LOST", "OK", null);
    }

    public ChatResponse addFollowUp(String msg) {
        return new ChatResponse("Followup added", "OK", null);
    }
}