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
        String msg = originalMsg.toLowerCase().trim();

        ChatResponse response;

        try {

            // =====================================
            // CANCEL / RESET
            // =====================================
            if (msg.contains("cancel") || msg.contains("reset") ||
                msg.contains("stop") || msg.equals("exit")) {

                waitingLeadInput = false;
                pendingLead = null;
                pendingDeleteList.clear();

                response = new ChatResponse(
                    "✅ Cancelled. How can I help you?",
                    "CANCELLED", null
                );
            }

            // =====================================
            // GREETING
            // =====================================
            else if (msg.contains("hi") || msg.contains("hello") ||
                     msg.contains("hey") || msg.contains("hii") ||
                     msg.contains("hiii") || msg.equals("h")) {

                waitingLeadInput = false;
                pendingLead = null;

                response = new ChatResponse(
                    "Hello 👋\n\nI am your AI CRM Assistant.\n\n" +
                    "You can say:\n" +
                    "• create lead for Ravi 9876543210 ravi@gmail.com Chennai\n" +
                    "• show leads\n" +
                    "• delete Ravi\n" +
                    "• update #1 email ravi@gmail.com\n" +
                    "• what is crm",
                    "GREETING", null
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
                        "Please provide missing details:\n\n" + missing,
                        "REQUIRED_FIELDS", null
                    );
                } else {
                    pendingLead.setRequirement("General");
                    pendingLead.setSource("CHAT");

                    Lead lead = leadService.createLead(pendingLead);

                    waitingLeadInput = false;
                    pendingLead = null;

                    response = new ChatResponse(
                        "✅ Lead created successfully!\n\n" +
                        "👤 Name  : " + lead.getName() + "\n" +
                        "📞 Phone : " + lead.getPhone() + "\n" +
                        "📧 Email : " + lead.getEmail() + "\n" +
                        "🏙 City  : " + lead.getCity(),
                        "LEAD_CREATED", lead
                    );
                }
            }

            // =====================================
            // DELETE CHOICE (number)
            // =====================================
            else if (msg.matches("\\d+") && !pendingDeleteList.isEmpty()) {

                int choice = Integer.parseInt(msg);

                if (choice >= 1 && choice <= pendingDeleteList.size()) {

                    Lead selected = pendingDeleteList.get(choice - 1);
                    boolean deleted = leadService.deleteLead(selected.getId());
                    pendingDeleteList.clear();

                    response = new ChatResponse(
                        deleted ? "🗑 Lead deleted successfully."
                                : "❌ Delete failed.",
                        deleted ? "DELETED" : "FAILED", null
                    );
                } else {
                    response = new ChatResponse(
                        "❌ Invalid option. Choose valid number.",
                        "INVALID_OPTION", null
                    );
                }
            }

            // =====================================
            // CREATE LEAD
            // =====================================
            else if (msg.contains("create") && msg.contains("lead")) {

                pendingLead = new LeadDto();
                pendingLead.setName(extractName(originalMsg));
                pendingLead.setPhone(extractPhone(originalMsg));
                pendingLead.setEmail(extractEmail(originalMsg));
                pendingLead.setCity(extractCity(originalMsg));

                String missing = getMissingFields();

                if (!missing.isBlank()) {
                    waitingLeadInput = true;
                    response = new ChatResponse(
                        "Please provide missing details:\n\n" + missing,
                        "REQUIRED_FIELDS", null
                    );
                } else {
                    pendingLead.setRequirement("General");
                    pendingLead.setSource("CHAT");

                    Lead lead = leadService.createLead(pendingLead);
                    pendingLead = null;

                    response = new ChatResponse(
                        "✅ Lead created successfully!\n\n" +
                        "👤 Name  : " + lead.getName() + "\n" +
                        "📞 Phone : " + lead.getPhone() + "\n" +
                        "📧 Email : " + lead.getEmail() + "\n" +
                        "🏙 City  : " + lead.getCity(),
                        "LEAD_CREATED", lead
                    );
                }
            }

            // =====================================
            // DELETE LEAD
            // =====================================
            else if (msg.startsWith("delete")) {

                String name = extractDeleteName(originalMsg);
                List<Lead> leads = leadService.findAllByName(name);

                if (leads.isEmpty()) {
                    response = new ChatResponse(
                        "❌ Lead not found: " + name,
                        "NOT_FOUND", null
                    );
                } else if (leads.size() == 1) {
                    boolean deleted = leadService.deleteLead(
                                        leads.get(0).getId());
                    response = new ChatResponse(
                        deleted ? "🗑 Lead deleted: " + name
                                : "❌ Delete failed.",
                        deleted ? "DELETED" : "FAILED", null
                    );
                } else {
                    pendingDeleteList = leads;
                    StringBuilder sb = new StringBuilder();
                    sb.append(leads.size())
                      .append(" leads found with name '")
                      .append(name)
                      .append("'.\n\nChoose one to delete:\n\n");
                    for (int i = 0; i < leads.size(); i++) {
                        Lead l = leads.get(i);
                        sb.append(i + 1).append(". ")
                          .append(l.getName()).append(" - ")
                          .append(l.getPhone()).append("\n");
                    }
                    response = new ChatResponse(
                        sb.toString(), "MULTIPLE_FOUND", null
                    );
                }
            }

            // =====================================
            // UPDATE BY LEAD ID (#1, #2...)
            // =====================================
            else if (msg.contains("update") && msg.contains("#")) {

                Pattern idPattern = Pattern.compile("#(\\d+)");
                Matcher idMatcher = idPattern.matcher(msg);

                if (!idMatcher.find()) {
                    response = new ChatResponse(
                        "Please say: update #1 email xxx@gmail.com",
                        "INVALID", null
                    );
                } else {
                    int leadNumber = Integer.parseInt(idMatcher.group(1));
                    List<Lead> allLeads = leadService.getAllLeads();

                    if (leadNumber < 1 || leadNumber > allLeads.size()) {
                        response = new ChatResponse(
                            "❌ Lead #" + leadNumber + " not found!",
                            "NOT_FOUND", null
                        );
                    } else {
                        Lead lead = allLeads.get(leadNumber - 1);
                        Long leadId = lead.getId();
                        String updateMsg = "❌ Unknown field to update.";
                        String action = "UNKNOWN";

                        if (msg.contains("email")) {
                            String email = extractEmail(originalMsg);
                            if (email.isEmpty()) {
                                updateMsg = "❌ Please provide valid email!";
                            } else if (!isValidEmail(email)) {
                                updateMsg = "❌ Invalid email format! Use: example@gmail.com";
                            } else {
                                leadService.updateFieldById(leadId, "email", email);
                                updateMsg = "✅ Email updated for Lead #" + leadNumber
                                          + "\nNew Email: " + email;
                                action = "UPDATED";
                            }
                        } else if (msg.contains("phone") || msg.contains("number")) {
                            String phone = extractPhone(originalMsg);
                            if (phone.isEmpty()) {
                                updateMsg = "❌ Please provide 10 digit phone!";
                            } else if (phone.length() != 10) {
                                updateMsg = "❌ Phone must be exactly 10 digits!";
                            } else {
                                leadService.updateFieldById(leadId, "phone", phone);
                                updateMsg = "✅ Phone updated for Lead #" + leadNumber
                                          + "\nNew Phone: " + phone;
                                action = "UPDATED";
                            }
                        } else if (msg.contains("name")) {
                            Pattern nameP = Pattern.compile(
                                "name\\s+to\\s+([a-zA-Z]+)",
                                Pattern.CASE_INSENSITIVE);
                            Matcher nameM = nameP.matcher(originalMsg);
                            if (nameM.find()) {
                                String newName = nameM.group(1).trim();
                                leadService.updateFieldById(leadId, "name", newName);
                                updateMsg = "✅ Name updated for Lead #" + leadNumber
                                          + "\nNew Name: " + newName;
                                action = "UPDATED";
                            } else {
                                updateMsg = "❌ Say: update #1 name to NewName";
                            }
                        } else if (msg.contains("city")) {
                            Pattern cityP = Pattern.compile(
                                "city\\s+to\\s+([a-zA-Z]+)",
                                Pattern.CASE_INSENSITIVE);
                            Matcher cityM = cityP.matcher(originalMsg);
                            if (cityM.find()) {
                                String newCity = cityM.group(1).trim();
                                leadService.updateFieldById(leadId, "city", newCity);
                                updateMsg = "✅ City updated for Lead #" + leadNumber
                                          + "\nNew City: " + newCity;
                                action = "UPDATED";
                            } else {
                                updateMsg = "❌ Say: update #1 city to Chennai";
                            }
                        } else if (msg.contains("status") || msg.contains("deal")) {
                            String newStatus = "PENDING";
                            if (msg.contains("won")) newStatus = "WON";
                            else if (msg.contains("lost")) newStatus = "LOST";
                            leadService.updateFieldById(leadId, "dealStatus", newStatus);
                            updateMsg = "✅ Status updated for Lead #" + leadNumber
                                      + "\nNew Status: " + newStatus;
                            action = "UPDATED";
                        } else if (msg.contains("follow")) {
                            Pattern followP = Pattern.compile(
                                "follow\\s*up\\s+to\\s+(.+)",
                                Pattern.CASE_INSENSITIVE);
                            Matcher followM = followP.matcher(originalMsg);
                            if (followM.find()) {
                                String newFollow = followM.group(1).trim();
                                leadService.updateFieldById(leadId, "followUp", newFollow);
                                updateMsg = "✅ Follow up updated for Lead #" + leadNumber
                                          + "\nFollow Up: " + newFollow;
                                action = "UPDATED";
                            } else {
                                updateMsg = "❌ Say: update #1 follow up to Call tomorrow";
                            }
                        }

                        response = new ChatResponse(updateMsg, action, null);
                    }
                }
            }

            // =====================================
            // CHANGE PHONE
            // =====================================
            else if (msg.contains("change") && msg.contains("phone")) {

                String name = extractUpdateName(originalMsg);
                String phone = extractPhone(originalMsg);

                if (phone.isEmpty() || phone.length() != 10) {
                    response = new ChatResponse(
                        "❌ Phone must be exactly 10 digits.",
                        "INVALID_PHONE", null
                    );
                } else {
                    boolean updated = leadService.updatePhoneByName(name, phone);
                    response = new ChatResponse(
                        updated ? "✅ Phone updated for: " + name
                                : "❌ Lead not found: " + name,
                        updated ? "UPDATED" : "NOT_FOUND", null
                    );
                }
            }

            // =====================================
            // WON
            // =====================================
            else if (msg.contains("won") || msg.contains("win")) {

                String name = msg.replace("won", "").replace("win", "").trim();
                boolean updated = leadService.updateDealStatus(name, "WON");
                response = new ChatResponse(
                    updated ? "🏆 Deal marked as WON for: " + name
                            : "❌ Lead not found: " + name,
                    updated ? "WON" : "NOT_FOUND", null
                );
            }

            // =====================================
            // LOST
            // =====================================
            else if (msg.contains("lost") || msg.contains("loss")) {

                String name = msg.replace("lost", "").replace("loss", "").trim();
                boolean updated = leadService.updateDealStatus(name, "LOST");
                response = new ChatResponse(
                    updated ? "❌ Deal marked as LOST for: " + name
                            : "❌ Lead not found: " + name,
                    updated ? "LOST" : "NOT_FOUND", null
                );
            }

            // =====================================
            // FOLLOW UP
            // =====================================
            else if (msg.contains("follow up") || msg.contains("followup")) {

                String[] parts = originalMsg.split("for");
                if (parts.length >= 2) {
                    String note = parts[0].replaceAll(
                        "(?i)follow up|followup", "").trim();
                    String name = parts[1].trim();
                    boolean updated = leadService.updateFollowUp(name, note);
                    response = new ChatResponse(
                        updated ? "📅 Follow up added for: " + name
                                : "❌ Lead not found: " + name,
                        updated ? "FOLLOWUP_ADDED" : "NOT_FOUND", null
                    );
                } else {
                    response = new ChatResponse(
                        "Please say: follow up [note] for [name]",
                        "INVALID", null
                    );
                }
            }

            // =====================================
            // SHOW LEADS
            // =====================================
            else if (msg.contains("show lead") || msg.contains("list lead")) {

                List<Lead> leads = leadService.getAllLeads();

                if (leads.isEmpty()) {
                    response = new ChatResponse(
                        "No leads available.", "EMPTY", null
                    );
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📋 Total Leads: ").append(leads.size()).append("\n\n");
                    int i = 1;
                    for (Lead l : leads) {
                        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
                          .append("🔢 Lead #").append(i++).append("\n")
                          .append("👤 Name     : ")
                          .append(l.getName() != null ? l.getName() : "-").append("\n")
                          .append("📞 Phone    : ")
                          .append(l.getPhone() != null ? l.getPhone() : "-").append("\n")
                          .append("📧 Email    : ")
                          .append(l.getEmail() != null ? l.getEmail() : "-").append("\n")
                          .append("🏙 City     : ")
                          .append(l.getCity() != null ? l.getCity() : "-").append("\n")
                          .append("💼 Deal     : ")
                          .append(l.getDealStatus() != null ? l.getDealStatus() : "PENDING").append("\n")
                          .append("📅 Follow Up: ")
                          .append(l.getFollowUp() != null ? l.getFollowUp() : "None").append("\n");
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━");
                    response = new ChatResponse(sb.toString(), "SHOW_LEADS", leads);
                }
            }

            // =====================================
            // DEFAULT — AI (Groq)
            // =====================================
            else {

                String aiReply = "AI service unavailable.";

                if (aiService != null) {
                    aiReply = aiService.askGroq(originalMsg);
                }

                response = new ChatResponse(aiReply, "AI_REPLY", null);
            }

            saveChatHistory(request, response.getReply());
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse(
                "Error: " + e.getMessage(), "ERROR", null
            );
        }
    }

    // =====================================
    // FILL MISSING FIELDS
    // =====================================
    private void fillMissingLeadFields(String text) {

        if (isBlank(pendingLead.getName())) {
            String name = extractName(text);
            if (!name.isBlank()) {
                pendingLead.setName(name);
                return;
            }
        }

        if (isBlank(pendingLead.getPhone())) {
            String phone = extractPhone(text);
            if (!phone.isBlank()) {
                pendingLead.setPhone(phone);
                return;
            }
        }

        if (isBlank(pendingLead.getEmail())) {
            String email = extractEmail(text);
            if (!email.isBlank()) {
                pendingLead.setEmail(email);
                return;
            }
        }

        if (isBlank(pendingLead.getCity())) {
            String city = extractCity(text);
            if (!city.isBlank()) {
                pendingLead.setCity(city);
            } else if (text.trim().matches("[a-zA-Z]+")) {
                pendingLead.setCity(text.trim());
            }
        }
    }

    // =====================================
    // VALIDATION — All 4 fields required
    // =====================================
    private String getMissingFields() {

        StringBuilder missing = new StringBuilder();

        // Name
        if (isBlank(pendingLead.getName()))
            missing.append("• Name\n");

        // Phone — must be 10 digits
        if (isBlank(pendingLead.getPhone())) {
            missing.append("• Phone number (10 digits)\n");
        } else if (pendingLead.getPhone().length() != 10) {
            missing.append("• Phone must be exactly 10 digits! " +
                          "(entered: " + pendingLead.getPhone().length() + " digits)\n");
            pendingLead.setPhone(null); // reset
        }

        // Email — must be valid format
        if (isBlank(pendingLead.getEmail())) {
            missing.append("• Email (example@gmail.com)\n");
        } else if (!isValidEmail(pendingLead.getEmail())) {
            missing.append("• Valid Email format! Use: example@gmail.com\n");
            pendingLead.setEmail(null); // reset
        }

        // City
        if (isBlank(pendingLead.getCity()))
            missing.append("• City\n");

        return missing.toString().trim();
    }

    // =====================================
    // HELPERS
    // =====================================
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        Pattern p = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return p.matcher(email.trim()).matches();
    }

    private String extractName(String text) {
        // "for Ravi" pattern
        Pattern p = Pattern.compile(
            "(?:for|name)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).trim();

        // First word after "lead"
        Pattern p2 = Pattern.compile(
            "lead\\s+([A-Za-z]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(text);
        if (m2.find()) return m2.group(1).trim();

        return "";
    }

    private String extractPhone(String text) {
        Pattern p = Pattern.compile("\\b\\d{10}\\b");
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }

    private String extractEmail(String text) {
        Pattern p = Pattern.compile(
            "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }

    private String extractCity(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String lower = text.toLowerCase();

        // Known cities
        if (lower.contains("chennai")) return "Chennai";
        if (lower.contains("madurai")) return "Madurai";
        if (lower.contains("coimbatore")) return "Coimbatore";
        if (lower.contains("bangalore")) return "Bangalore";
        if (lower.contains("mumbai")) return "Mumbai";
        if (lower.contains("delhi")) return "Delhi";
        if (lower.contains("hyderabad")) return "Hyderabad";
        if (lower.contains("kolkata")) return "Kolkata";
        if (lower.contains("pune")) return "Pune";
        if (lower.contains("erode")) return "Erode";
        if (lower.contains("salem")) return "Salem";
        if (lower.contains("trichy")) return "Trichy";
        if (lower.contains("vellore")) return "Vellore";
        if (lower.contains("tirunelveli")) return "Tirunelveli";
        if (lower.contains("tiruppur")) return "Tiruppur";

        // "in Chennai" pattern
        Pattern p = Pattern.compile(
            "\\bin\\s+([A-Z][a-z]+)\\b",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).trim();

        return "";
    }

    private String extractDeleteName(String text) {
        return text.replaceFirst("(?i)delete", "").trim();
    }

    private String extractUpdateName(String text) {
        Pattern p = Pattern.compile(
            "change\\s+(.+?)\\s+phone",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private void saveChatHistory(ChatRequest request, String reply) {
        try {
            ChatHistory history = new ChatHistory();
            history.setUserId(Long.parseLong(request.getUserId()));
            history.setSessionId(request.getSessionId());
            history.setMessage(request.getMessage());
            history.setResponse(reply);
            chatHistoryRepository.save(history);
        } catch (Exception ignored) {}
    }
}