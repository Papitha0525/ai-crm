package com.aicrm.backend.service;

import com.aicrm.backend.dto.ChatRequest;
import com.aicrm.backend.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private LeadChatService leadChatService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private AiChatService aiChatService;

    public ChatResponse processMessage(ChatRequest request) {

        try {

            String msg = request.getMessage().trim();
            String lower = msg.toLowerCase();

            String role =
                    roleService.getRole(
                            request.getUserId()
                    );

            ChatResponse response;

            // =========================
            // Greeting
            // =========================
            if (lower.equals("hi") ||
                lower.equals("hello") ||
                lower.equals("hey")) {

                response = new ChatResponse(
                        "Hello 👋 Welcome to AI CRM",
                        "GREETING",
                        null
                );
            }

            // =========================
            // Create Lead
            // =========================
            else if (lower.contains("create")
                    && lower.contains("lead")) {

                response =
                        leadChatService.createLead(msg);
            }

            // =========================
            // Show Leads
            // =========================
            else if (lower.contains("show leads")
                    || lower.contains("list leads")) {

                response =
                        leadChatService.showLeads(
                                role,
                                request.getUserId()
                        );
            }

            // =========================
            // Delete Lead
            // =========================
            else if (lower.startsWith("delete")) {

                response =
                        leadChatService.deleteLead(
                                role,
                                msg
                        );
            }

            // =========================
            // Update Phone
            // =========================
            else if (lower.contains("phone")
                    && lower.contains("change")) {

                response =
                        leadChatService.updatePhone(msg);
            }

            // =========================
            // Mark WON
            // =========================
            else if (lower.contains("won")) {

                response =
                        leadChatService.markWon(msg);
            }

            // =========================
            // Mark LOST
            // =========================
            else if (lower.contains("lost")) {

                response =
                        leadChatService.markLost(msg);
            }

            // =========================
            // Follow Up
            // =========================
            else if (lower.contains("follow up")) {

                response =
                        leadChatService.addFollowUp(msg);
            }

            // =========================
            // AI Chat
            // =========================
            else {

                response = new ChatResponse(
                        aiChatService.ask(msg),
                        "AI_REPLY",
                        null
                );
            }

            chatHistoryService.save(
                    request,
                    response
            );

            return response;

        } catch (Exception e) {

            return new ChatResponse(
                    "Error: " + e.getMessage(),
                    "ERROR",
                    null
            );
        }
    }
}