package com.aicrm.backend.service;

import com.aicrm.backend.dto.ChatRequest;
import com.aicrm.backend.dto.ChatResponse;
import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.ChatHistory;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.repository.ChatHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {

    @Autowired
    private LeadService leadService;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired(required = false)
    private AiOrchestratorService aiService;

    private List<Lead> pendingDeleteList = new ArrayList<>();
    private LeadDto pendingLead = null;
    private boolean waitingLeadInput = false;

    public ChatResponse processMessage(ChatRequest request) {

        String originalMsg = request.getMessage().trim();
        String msg = originalMsg.toLowerCase(Locale.ROOT).trim();

        String role = getRole(request);

        ChatResponse response;

        try {

            // =====================================
            // CANCEL
            // =====================================
            if (msg.equals("cancel") || msg.equals("reset")
                    || msg.equals("stop") || msg.equals("exit")) {

                waitingLeadInput = false;
                pendingLead = null;
                pendingDeleteList.clear();

                response = new ChatResponse(
                        "✅ Cancelled successfully.",
                        "CANCELLED",
                        null
                );
            }

            // =====================================
            // GREETING
            // =====================================
            else if (msg.equals("hi") || msg.equals("hello")
                    || msg.equals("hey")) {

                response = new ChatResponse(
                        "👋 Hello " + role + "\n\n"
                                + "Available commands:\n"
                                + "• create lead for Ravi 9876543210 ravi@gmail.com Chennai\n"
                                + "• show leads\n"
                                + "• show won deals\n"
                                + "• show lead Ravi\n"
                                + "• update #1 phone 9999999999",
                        "GREETING",
                        null
                );
            }

            // =====================================
            // CONTINUE CREATE LEAD FLOW
            // =====================================
            else if (waitingLeadInput) {

                fillMissingLeadFields(originalMsg);

                String missing = getMissingFields();

                if (!missing.isBlank()) {

                    response = new ChatResponse(
                            "Please provide:\n\n" + missing,
                            "REQUIRED",
                            null
                    );

                } else {

                    pendingLead.setRequirement("General");
                    pendingLead.setSource("CHAT");
                    pendingLead.setDealStatus("PENDING");
                    pendingLead.setStatus("NEW");

                    Lead lead = leadService.createLead(pendingLead);

                    waitingLeadInput = false;
                    pendingLead = null;

                    response = new ChatResponse(
                            "✅ Lead created successfully\n\n"
                                    + "👤 " + lead.getName()
                                    + "\n📞 " + lead.getPhone()
                                    + "\n📧 " + lead.getEmail()
                                    + "\n🏙 " + lead.getCity(),
                            "CREATED",
                            lead
                    );
                }
            }

            // =====================================
            // CREATE LEAD
            // =====================================
            else if (msg.matches(".*create.*lead.*")){

                pendingLead = new LeadDto();

                pendingLead.setName(extractName(originalMsg));
                pendingLead.setPhone(extractPhone(originalMsg));
                pendingLead.setEmail(extractEmail(originalMsg));
                pendingLead.setCity(extractCity(originalMsg));
                pendingLead.setRequirement(extractRequirement(originalMsg));

                String missing = getMissingFields();

                if (!missing.isBlank()) {

                    waitingLeadInput = true;

                    response = new ChatResponse(
                            "Need these details:\n\n" + missing,
                            "REQUIRED",
                            null
                    );

                } else {

                    if (isBlank(pendingLead.getRequirement())) {
                        pendingLead.setRequirement("General");
                    }

                    pendingLead.setSource("CHAT");
                    pendingLead.setDealStatus("PENDING");
                    pendingLead.setStatus("NEW");

                    Lead lead = leadService.createLead(pendingLead);

                    pendingLead = null;

                    response = new ChatResponse(
                            "✅ Lead created successfully\n\n"
                                    + "👤 " + lead.getName(),
                            "CREATED",
                            lead
                    );
                }
            }

            // =====================================
            // ADMIN ONLY DELETE
            // =====================================
            else if (msg.startsWith("delete")) {

                if (!role.equals("ADMIN")) {

                    response = new ChatResponse(
                            "❌ Only ADMIN can delete leads.",
                            "DENIED",
                            null
                    );

                } else {

                    String name = extractDeleteName(originalMsg);
                    List<Lead> leads = leadService.findAllByName(name);
                    if (leads.isEmpty()) {

                        response = new ChatResponse(
                                "❌ Lead not found : " + name,
                                "NOT_FOUND",
                                null
                        );

                    } else if (leads.size() == 1) {

                        boolean deleted =
                                leadService.deleteLead(leads.get(0).getId());

                        response = new ChatResponse(
                                deleted
                                        ? "🗑 Lead deleted successfully."
                                        : "❌ Delete failed.",
                                deleted ? "DELETED" : "FAILED",
                                null
                        );

                    } else {

                        pendingDeleteList = leads;

                        StringBuilder sb = new StringBuilder();
                        sb.append("Multiple leads found:\n\n");

                        for (int i = 0; i < leads.size(); i++) {
                            Lead l = leads.get(i);

                            sb.append(i + 1)
                                    .append(". ")
                                    .append(l.getName())
                                    .append(" - ")
                                    .append(l.getPhone())
                                    .append("\n");
                        }

                        sb.append("\nReply with number to delete.");

                        response = new ChatResponse(
                                sb.toString(),
                                "MULTIPLE_FOUND",
                                null
                        );
                    }
                }
            }

            // =====================================
            // DELETE CHOICE
            // =====================================
            else if (msg.matches("\\d+") && !pendingDeleteList.isEmpty()) {

                if (!role.equals("ADMIN")) {

                    response = new ChatResponse(
                            "❌ Only ADMIN can delete.",
                            "DENIED",
                            null
                    );

                } else {

                    int choice = Integer.parseInt(msg);

                    if (choice >= 1 && choice <= pendingDeleteList.size()) {

                        Lead selected =
                                pendingDeleteList.get(choice - 1);

                        boolean deleted =
                                leadService.deleteLead(selected.getId());

                        pendingDeleteList.clear();

                        response = new ChatResponse(
                                deleted
                                        ? "🗑 Lead deleted."
                                        : "❌ Delete failed.",
                                deleted ? "DELETED" : "FAILED",
                                null
                        );

                    } else {

                        response = new ChatResponse(
                                "❌ Invalid number.",
                                "INVALID",
                                null
                        );
                    }
                }
            }

            // =====================================
            // SHOW BY NAME
            // =====================================
            else if (msg.startsWith("show lead ")) {

                String name =
                        originalMsg.substring(10).trim();

                List<Lead> leads =
                        leadService.findAllByName(name);

                response = buildLeadResponse(leads, role);
            }

            // =====================================
            // SHOW WON DEALS
            // =====================================
            else if (msg.contains("show won")) {

                List<Lead> leads =
                        leadService.getWonLeads();

                response = buildLeadResponse(
                        filterByRole(leads, request, role),
                        role
                );
            }

            // =====================================
            // SHOW LOST DEALS
            // =====================================
            else if (msg.contains("show lost")) {

                List<Lead> leads =
                        leadService.getLostLeads();

                response = buildLeadResponse(
                        filterByRole(leads, request, role),
                        role
                );
            }

            // =====================================
            // SHOW PENDING DEALS
            // =====================================
            else if (msg.contains("show pending")) {

                List<Lead> leads =
                        leadService.getAllLeads();

                List<Lead> pending = new ArrayList<>();

                for (Lead l : leads) {
                    if ("PENDING".equalsIgnoreCase(
                            l.getDealStatus())) {
                        pending.add(l);
                    }
                }

                response = buildLeadResponse(
                        filterByRole(pending, request, role),
                        role
                );
            }

            // =====================================
            // SHOW LEADS
            // =====================================
            else if (msg.equals("show leads")
                    || msg.equals("list leads")) {

                List<Lead> leads;

                if (role.equals("ADMIN")) {

                    leads = leadService.getAllLeads();

                } else if (role.equals("SALESMAN")) {

                    leads = leadService.getAssignedLeads(
                            request.getUserId()
                    );

                } else {

                    response = new ChatResponse(
                            "❌ USER cannot see all leads.",
                            "DENIED",
                            null
                    );

                    saveChatHistory(
                            request,
                            response.getReply()
                    );

                    return response;
                }

                response = buildLeadResponse(leads, role);
            }

            // =====================================
            // UPDATE LEAD
            // =====================================
            else if (msg.contains("update")
                    && msg.contains("#")) {
                Pattern p = Pattern.compile("#(\\d+)");
                Matcher m = p.matcher(msg);

                if (!m.find()) {

                    response = new ChatResponse(
                            "Use: update #1 phone 9999999999",
                            "INVALID",
                            null
                    );

                } else {

                    int no = Integer.parseInt(m.group(1));

                    List<Lead> leads = role.equals("ADMIN")
                            ? leadService.getAllLeads()
                            : leadService.getAssignedLeads(
                                    request.getUserId());

                    if (no < 1 || no > leads.size()) {

                        response = new ChatResponse(
                                "❌ Lead not found.",
                                "NOT_FOUND",
                                null
                        );

                    } else {

                        Lead lead = leads.get(no - 1);

                        if (msg.contains("phone")) {

                            String phone =
                                    extractPhone(originalMsg);

                            leadService.updateFieldById(
                                    lead.getId(),
                                    "phone",
                                    phone
                            );

                            response = new ChatResponse(
                                    "✅ Phone updated.",
                                    "UPDATED",
                                    null
                            );

                        } else if (msg.contains("email")) {

                            String email =
                                    extractEmail(originalMsg);

                            leadService.updateFieldById(
                                    lead.getId(),
                                    "email",
                                    email
                            );

                            response = new ChatResponse(
                                    "✅ Email updated.",
                                    "UPDATED",
                                    null
                            );

                        } else {

                            response = new ChatResponse(
                                    "❌ Unknown field.",
                                    "INVALID",
                                    null
                            );
                        }
                    }
                }
            }

            // =====================================
            // DEFAULT AI
            // =====================================
            else {

                String aiReply =
                        "AI unavailable.";

                if (aiService != null) {
                    aiReply =
                            aiService.askGroq(originalMsg);
                }

                response = new ChatResponse(
                        aiReply,
                        "AI_REPLY",
                        null
                );
            }

            saveChatHistory(
                    request,
                    response.getReply()
            );

            return response;

        } catch (Exception e) {

            e.printStackTrace();

            return new ChatResponse(
                    "Error : " + e.getMessage(),
                    "ERROR",
                    null
            );
        }
    }

    // =====================================
    // ROLE
    // =====================================
    private String getRole(ChatRequest request) {

        if ("1".equals(request.getUserId()))
            return "ADMIN";

        if ("2".equals(request.getUserId()))
            return "SALESMAN";

        return "USER";
    }

    // =====================================
    // RESPONSE BUILDER
    // =====================================
    private ChatResponse buildLeadResponse(
            List<Lead> leads,
            String role) {

        if (leads == null || leads.isEmpty()) {

            return new ChatResponse(
                    "No leads found.",
                    "EMPTY",
                    null
            );
        }

        StringBuilder sb = new StringBuilder();

        sb.append("📋 Leads : ")
          .append(leads.size())
          .append("\n\n");

        int i = 1;

        for (Lead l : leads) {

            sb.append("#").append(i++).append("\n");
            sb.append("👤 ").append(l.getName()).append("\n");

            if (!role.equals("USER")) {
                sb.append("📞 ").append(nvl(l.getPhone())).append("\n");
                sb.append("📧 ").append(nvl(l.getEmail())).append("\n");
            }

            sb.append("🏙 ").append(nvl(l.getCity())).append("\n");
            sb.append("💼 ").append(nvl(l.getDealStatus())).append("\n");
            sb.append("──────────────\n");
        }

        return new ChatResponse(
                sb.toString(),
                "SHOW",
                leads
        );
    }

    private List<Lead> filterByRole(
            List<Lead> leads,
            ChatRequest request,
            String role) {

        if (role.equals("ADMIN"))
            return leads;

        if (role.equals("SALESMAN"))
            return leadService.getAssignedLeads(
                    request.getUserId());

        return new ArrayList<>();
    }

    private String nvl(String v) {
        return v == null ? "-" : v;
    }

    // =====================================
    // HELPERS
    // =====================================
    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String getMissingFields() {

        StringBuilder sb = new StringBuilder();

        if (isBlank(pendingLead.getName()))
            sb.append("• Name\n");

        if (isBlank(pendingLead.getPhone()))
            sb.append("• Phone\n");

        if (isBlank(pendingLead.getEmail()))
            sb.append("• Email\n");

        if (isBlank(pendingLead.getCity()))
            sb.append("• City\n");

        if (isBlank(pendingLead.getRequirement()))
        sb.append("• Requirement\n");

        return sb.toString();
    }

    private void fillMissingLeadFields(String t) {

        if (isBlank(pendingLead.getName()))
            pendingLead.setName(extractName(t));

        if (isBlank(pendingLead.getPhone()))
            pendingLead.setPhone(extractPhone(t));

        if (isBlank(pendingLead.getEmail()))
            pendingLead.setEmail(extractEmail(t));

        if (isBlank(pendingLead.getCity()))
            pendingLead.setCity(extractCity(t));

        if (isBlank(pendingLead.getRequirement()))
        pendingLead.setRequirement(t.trim());
    }

    private String extractName(String text) {
        Matcher m = Pattern.compile(
                "(?i)for\\s+([a-zA-Z]+)")
                .matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private String extractPhone(String text) {
        Matcher m = Pattern.compile("\\d{10}")
                .matcher(text);
        return m.find() ? m.group() : "";
    }

    private String extractEmail(String text) {
        Matcher m = Pattern.compile(
                "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+")
                .matcher(text);
        return m.find() ? m.group() : "";
    }

    private String extractCity(String text) {

        String[] cities = {
                "chennai","madurai","coimbatore",
                "erode","salem","trichy"
        };

        for (String c : cities) {
            if (text.toLowerCase().contains(c))
                return c.substring(0,1).toUpperCase()
                        + c.substring(1);
        }

        return "";
    }

    private String extractRequirement(String text) {
        return "General";
    }

    private String extractDeleteName(String text) {
        return text.replaceFirst(
                "(?i)delete", "").trim();
    }

    private void saveChatHistory(
            ChatRequest request,
            String reply) {

        try {

            ChatHistory h = new ChatHistory();

            h.setUserId(
                    Long.parseLong(
                            request.getUserId()));

            h.setSessionId(
                    request.getSessionId());

            h.setMessage(
                    request.getMessage());

            h.setResponse(reply);

            chatHistoryRepository.save(h);

        } catch (Exception ignored) {
        }
    }
}