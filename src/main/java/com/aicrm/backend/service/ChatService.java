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

    // duplicate delete memory
    private List<Lead> pendingDeleteList = new ArrayList<>();

    // create lead memory
    private LeadDto pendingLead = null;
    private boolean waitingLeadInput = false;

    public ChatResponse processMessage(ChatRequest request) {

        String originalMsg = request.getMessage().trim();
        String msg = originalMsg.toLowerCase().trim();

        ChatResponse response;

        try {

            // =====================================
            // GREETING
            // =====================================
            if (msg.equals("hi") || msg.equals("hello") || msg.equals("hey")) {

                response = new ChatResponse(
                        "Hello 👋\n\nHow can I help you today?",
                        "GREETING",
                        null
                );
            }
// =====================================
// UPDATE BY LEAD ID (#1, #2, etc.)
// =====================================
else if (msg.contains("update") && 
         msg.contains("#")) {

    try {
        // #1 இருந்து ID எடு
        Pattern idPattern = Pattern.compile("#(\\d+)");
        Matcher idMatcher = idPattern.matcher(msg);

        if (!idMatcher.find()) {
            response = new ChatResponse(
                "Please say: update #1 email xxx@gmail.com",
                "INVALID", null);
        } else {
            int leadNumber = Integer.parseInt(
                idMatcher.group(1));

            // All leads எடு
            List<Lead> allLeads = 
                leadService.getAllLeads();

            if (leadNumber < 1 || 
                leadNumber > allLeads.size()) {
                response = new ChatResponse(
                    "❌ Lead #" + leadNumber + 
                    " not found!",
                    "NOT_FOUND", null);
            } else {
                // Lead ID எடு
                Lead lead = allLeads.get(
                    leadNumber - 1);
                Long leadId = lead.getId();

                // Field detect பண்ணு
                String updateMsg = 
                    "❌ Unknown field to update.";
                String action = "UNKNOWN";

                // Email update
                if (msg.contains("email")) {
                    String email = 
                        extractEmail(originalMsg);
                    if (email.isEmpty()) {
                        updateMsg = "❌ Please provide " +
                            "valid email: xxx@gmail.com";
                    } else if (!isValidEmail(email)) {
                        updateMsg = "❌ Invalid email! " +
                            "Use: example@gmail.com";
                    } else {
                        leadService.updateFieldById(
                            leadId, "email", email);
                        updateMsg = "✅ Email updated " +
                            "for Lead #" + leadNumber +
                            "\nNew Email: " + email;
                        action = "UPDATED";
                    }
                }

                // Phone update
                else if (msg.contains("phone") || 
                         msg.contains("number")) {
                    String phone = 
                        extractPhone(originalMsg);
                    if (phone.isEmpty()) {
                        updateMsg = "❌ Please provide " +
                            "10 digit phone number!";
                    } else if (phone.length() != 10) {
                        updateMsg = "❌ Phone must be " +
                            "exactly 10 digits!";
                    } else {
                        leadService.updateFieldById(
                            leadId, "phone", phone);
                        updateMsg = "✅ Phone updated " +
                            "for Lead #" + leadNumber +
                            "\nNew Phone: " + phone;
                        action = "UPDATED";
                    }
                }

                // Name update
                else if (msg.contains("name")) {
                    Pattern nameP = Pattern.compile(
                        "name\\s+to\\s+([a-zA-Z]+)",
                        Pattern.CASE_INSENSITIVE);
                    Matcher nameM = 
                        nameP.matcher(originalMsg);
                    if (nameM.find()) {
                        String newName = 
                            nameM.group(1).trim();
                        leadService.updateFieldById(
                            leadId, "name", newName);
                        updateMsg = "✅ Name updated " +
                            "for Lead #" + leadNumber +
                            "\nNew Name: " + newName;
                        action = "UPDATED";
                    } else {
                        updateMsg = "❌ Say: update #1 " +
                            "name to NewName";
                    }
                }

                // City update
                else if (msg.contains("city")) {
                    Pattern cityP = Pattern.compile(
                        "city\\s+to\\s+([a-zA-Z]+)",
                        Pattern.CASE_INSENSITIVE);
                    Matcher cityM = 
                        cityP.matcher(originalMsg);
                    if (cityM.find()) {
                        String newCity = 
                            cityM.group(1).trim();
                        leadService.updateFieldById(
                            leadId, "city", newCity);
                        updateMsg = "✅ City updated " +
                            "for Lead #" + leadNumber +
                            "\nNew City: " + newCity;
                        action = "UPDATED";
                    } else {
                        updateMsg = "❌ Say: update #1 " +
                            "city to Chennai";
                    }
                }

                // Status update (WON/LOST)
                else if (msg.contains("status") ||
                         msg.contains("deal")) {
                    String newStatus = "PENDING";
                    if (msg.contains("won")) 
                        newStatus = "WON";
                    else if (msg.contains("lost")) 
                        newStatus = "LOST";
                    leadService.updateFieldById(
                        leadId, "dealStatus", newStatus);
                    updateMsg = "✅ Deal status updated " +
                        "for Lead #" + leadNumber +
                        "\nNew Status: " + newStatus;
                    action = "UPDATED";
                }

                // Follow up update
                else if (msg.contains("follow")) {
                    Pattern followP = Pattern.compile(
                        "follow\\s*up\\s+to\\s+(.+)",
                        Pattern.CASE_INSENSITIVE);
                    Matcher followM = 
                        followP.matcher(originalMsg);
                    if (followM.find()) {
                        String newFollow = 
                            followM.group(1).trim();
                        leadService.updateFieldById(
                            leadId, "followUp", newFollow);
                        updateMsg = "✅ Follow up updated " +
                            "for Lead #" + leadNumber +
                            "\nFollow Up: " + newFollow;
                        action = "UPDATED";
                    } else {
                        updateMsg = "❌ Say: update #1 " +
                            "follow up to Call tomorrow";
                    }
                }

                response = new ChatResponse(
                    updateMsg, action, null);
            }
        }
    } catch (Exception ex) {
        response = new ChatResponse(
            "❌ Error: " + ex.getMessage(),
            "ERROR", null);
    }
}
            // =====================================
            // CONTINUE CREATE FLOW
            // =====================================
            
            else if (waitingLeadInput) {

                fillMissingLeadFields(originalMsg);

                String missing = getMissingFields();

                if (!missing.isBlank()) {

                    response = new ChatResponse(
                            "Required fields missing:\n\n" + missing,
                            "REQUIRED_FIELDS",
                            null
                    );

                } else {

                    if (pendingLead.getPhone().length() != 10) {

                        response = new ChatResponse(
                                "Phone number must be exactly 10 digits.",
                                "INVALID_PHONE",
                                null
                        );

                    } else {

                        pendingLead.setRequirement("General");
                        pendingLead.setSource("CHAT");

                        Lead lead = leadService.createLead(pendingLead);

                        waitingLeadInput = false;
                        pendingLead = null;

                        response = new ChatResponse(
                                "✅ Lead created successfully.\n\nName: " + lead.getName(),
                                "LEAD_CREATED",
                                lead
                        );
                    }
                }
            }

            // =====================================
            // DELETE CHOICE 1 / 2 / 3
            // =====================================
            else if (msg.matches("\\d+")) {

                int choice = Integer.parseInt(msg);

                if (!pendingDeleteList.isEmpty()) {

                    if (choice >= 1 && choice <= pendingDeleteList.size()) {

                        Lead selected = pendingDeleteList.get(choice - 1);

                        boolean deleted =
                                leadService.deleteLead(selected.getId());

                        pendingDeleteList.clear();

                        response = new ChatResponse(
                                deleted
                                        ? "🗑 Lead deleted successfully."
                                        : "❌ Delete failed.",
                                deleted ? "DELETED" : "FAILED",
                                null
                        );

                    } else {

                        response = new ChatResponse(
                                "Invalid option. Please choose valid number.",
                                "INVALID_OPTION",
                                null
                        );
                    }

                } else {

                    response = new ChatResponse(
                            "No pending selection.",
                            "NO_PENDING",
                            null
                    );
                }
            }

            // =====================================
            // CREATE LEAD
            // =====================================
            else if (msg.contains("create") &&
         msg.contains("lead")) {

    pendingLead = new LeadDto();
    pendingLead.setName(extractName(originalMsg));
    pendingLead.setPhone(extractPhone(originalMsg));
    pendingLead.setEmail(extractEmail(originalMsg));

    // ✅ City extract பண்ணு
    String city = extractCity(originalMsg);
    pendingLead.setCity(
        city.isEmpty() ? null : city); // ← null set பண்ணு

    String missing = getMissingFields();

    if (!missing.isBlank()) {
        waitingLeadInput = true;
        response = new ChatResponse(
            "Please provide missing details:\n\n"
            + missing,
            "REQUIRED_FIELDS", null);
    } else if (pendingLead.getPhone() != null &&
               pendingLead.getPhone().length() != 10) {
        response = new ChatResponse(
            "Phone number must be exactly 10 digits.",
            "INVALID_PHONE", null);
    } else {
        pendingLead.setRequirement("General");
        pendingLead.setSource("CHAT");
        Lead lead = leadService.createLead(pendingLead);
        pendingLead = null;
        response = new ChatResponse(
            "✅ Lead created successfully.\n\nName: "
            + lead.getName(),
            "LEAD_CREATED", lead);
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
                            "❌ Lead not found:\n\n" + name,
                            "NOT_FOUND",
                            null
                    );

                } else if (leads.size() == 1) {

                    boolean deleted =
                            leadService.deleteLead(leads.get(0).getId());

                    response = new ChatResponse(
                            deleted
                                    ? "🗑 Lead deleted:\n\n" + name
                                    : "❌ Delete failed.",
                            deleted ? "DELETED" : "FAILED",
                            null
                    );

                } else {

                    pendingDeleteList = leads;

                    StringBuilder sb = new StringBuilder();

                    sb.append(leads.size())
                      .append(" leads found with name ")
                      .append(name)
                      .append(".\n\nChoose one to delete:\n\n");

                    for (int i = 0; i < leads.size(); i++) {

                        Lead l = leads.get(i);

                        sb.append(i + 1)
                          .append(". ")
                          .append(l.getName())
                          .append(" - ")
                          .append(l.getPhone())
                          .append("\n");
                    }

                    response = new ChatResponse(
                            sb.toString(),
                            "MULTIPLE_FOUND",
                            null
                    );
                }
            }

            // =====================================
            // UPDATE PHONE
            // =====================================
            else if (msg.contains("change") && msg.contains("phone")) {

                String name = extractUpdateName(originalMsg);
                String phone = extractPhone(originalMsg);

                if (phone.length() != 10) {

                    response = new ChatResponse(
                            "Phone number must be exactly 10 digits.",
                            "INVALID_PHONE",
                            null
                    );

                } else {

                    boolean updated =
                            leadService.updatePhoneByName(name, phone);

                    response = new ChatResponse(
                            updated
                                    ? "✅ Phone updated for:\n\n" + name
                                    : "❌ Lead not found:\n\n" + name,
                            updated ? "UPDATED" : "NOT_FOUND",
                            null
                    );
                }
            }
            
            // =====================================
// UPDATE DEAL STATUS - WON
// =====================================
else if (msg.contains("won") || msg.contains("win")) {
    String name = msg.replace("won", "")
                     .replace("win", "").trim();
    boolean updated = leadService
                        .updateDealStatus(name, "WON");
    response = new ChatResponse(
        updated
            ? "🏆 Deal marked as WON for: " + name
            : "❌ Lead not found: " + name,
        updated ? "WON" : "NOT_FOUND",
        null
    );
}

// =====================================
// UPDATE DEAL STATUS - LOST
// =====================================
else if (msg.contains("lost") || msg.contains("loss")) {
    String name = msg.replace("lost", "")
                     .replace("loss", "").trim();
    boolean updated = leadService
                        .updateDealStatus(name, "LOST");
    response = new ChatResponse(
        updated
            ? "❌ Deal marked as LOST for: " + name
            : "❌ Lead not found: " + name,
        updated ? "LOST" : "NOT_FOUND",
        null
    );
}

// =====================================
// ADD FOLLOW UP
// =====================================
else if (msg.contains("follow up") || 
         msg.contains("followup")) {
    String[] parts = originalMsg.split("for");
    if (parts.length >= 2) {
        String note = parts[0].replaceAll(
            "(?i)follow up|followup", "").trim();
        String name = parts[1].trim();
        boolean updated = leadService
                            .updateFollowUp(name, note);
        response = new ChatResponse(
            updated
                ? "📅 Follow up added for: " + name
                : "❌ Lead not found: " + name,
            updated ? "FOLLOWUP_ADDED" : "NOT_FOUND",
            null
        );
    } else {
        response = new ChatResponse(
            "Please say: follow up [note] for [name]",
            "INVALID",
            null
        );
    }
}

            // =====================================
            // SHOW LEADS
            // =====================================
            else if (msg.contains("show leads") ||
         msg.contains("list leads")) {
    List<Lead> leads = leadService.getAllLeads();
    if (leads.isEmpty()) {
        response = new ChatResponse(
            "No leads available.", "EMPTY", null);
    } else {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 Total Leads: ")
          .append(leads.size())
          .append("\n\n");
        int i = 1;
        for (Lead lead : leads) {
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
              .append("🔢 Lead #").append(i++).append("\n")
              .append("👤 Name     : ").append(
                lead.getName() != null
                ? lead.getName() : "-").append("\n")
              .append("📞 Phone    : ").append(
                lead.getPhone() != null
                ? lead.getPhone() : "-").append("\n")
              .append("📧 Email    : ").append(
                lead.getEmail() != null
                ? lead.getEmail() : "-").append("\n")
              .append("🏙 City     : ").append(
                lead.getCity() != null
                ? lead.getCity() : "-").append("\n")
              .append("📌 Source   : ").append(
                lead.getSource() != null
                ? lead.getSource() : "-").append("\n")
              .append("🔄 Status   : ").append(
                lead.getStatus() != null
                ? lead.getStatus() : "-").append("\n")
              .append("💼 Deal     : ").append(
                lead.getDealStatus() != null
                ? lead.getDealStatus() : "PENDING").append("\n")
              .append("📅 Follow Up: ").append(
                lead.getFollowUp() != null
                ? lead.getFollowUp() : "None").append("\n")
              .append("🕐 Created  : ").append(
                lead.getCreatedAt() != null
                ? lead.getCreatedAt()
                       .toString().substring(0, 10)
                : "-").append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━");
        response = new ChatResponse(
            sb.toString(), "SHOW_LEADS", leads);
    }
}

            // =====================================
            // CRM AI ONLY
            // =====================================
            else {

                if (!isCrmRelated(msg)) {

                    response = new ChatResponse(
                            "I handle only CRM related requests.",
                            "NON_CRM",
                            null
                    );

                } else {

                    String aiReply = "CRM AI unavailable.";

                    if (aiService != null) {
                        aiReply = aiService.askGroq(originalMsg);
                    }

                    response = new ChatResponse(
                            formatReply(aiReply),
                            "AI_REPLY",
                            null
                    );
                }
            }

            saveChatHistory(request, response.getReply());

            return response;

        } catch (Exception e) {

            return new ChatResponse(
                    "Error:\n\n" + e.getMessage(),
                    "ERROR",
                    null
            );
        }
    }

    // =====================================
    // CREATE HELPERS
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
            }
        }

        if (isBlank(pendingLead.getCity())) {
    String city = extractCity(text);
    if (!city.isBlank()) {
        pendingLead.setCity(city);
    } else {
        // City keyword இல்லன்னா direct-ஆ set பண்ணு
        // Example: user "Chennai" மட்டும் type பண்ணினா
        String trimmed = text.trim();
        if (trimmed.matches("[a-zA-Z]+")) {
            pendingLead.setCity(trimmed);
        }
    }
}
    }

  private String getMissingFields() {

    StringBuilder missing = new StringBuilder();

    if (isBlank(pendingLead.getName()))
        missing.append("• Please provide Name\n");

    if (isBlank(pendingLead.getPhone()))
        missing.append("• Please provide Phone (10 digits)\n");

    if (isBlank(pendingLead.getEmail())) {
        missing.append("• Please provide Email " +
                       "(example@gmail.com)\n");
    } else if (!isValidEmail(pendingLead.getEmail())) {
        missing.append("• Invalid Email! " +
                       "Use: example@gmail.com\n");
        pendingLead.setEmail("");
    }

    // ✅ இதை add பண்ணு
    if (isBlank(pendingLead.getCity()))
        missing.append("• Please provide City\n");

    return missing.toString().trim();
}
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }private boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty())
        return false;
    Pattern p = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    return p.matcher(email.trim()).matches();
}
 

    // =====================================
    // OTHER HELPERS
    // =====================================

    private boolean isCrmRelated(String text) {

        String[] crmWords = {
                "crm", "lead", "customer", "sales",
                "client", "follow", "task", "report",
                "update", "delete", "create", "phone",
                "email", "city", "business"
        };

        for (String word : crmWords) {
            if (text.contains(word)) return true;
        }

        return false;
    }

    private String formatReply(String text) {
        return text.replace(". ", ".\n\n").replace(": ", ":\n");
    }

    private String extractName(String text) {

        Pattern p = Pattern.compile(
                "name\\s+(.+?)(?=\\s+phone|\\s+number|\\s+email|\\s+city|$)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(text);

        return m.find() ? m.group(1).trim() : "";
    }

    private String extractPhone(String text) {

        Pattern p = Pattern.compile("\\b\\d{10}\\b");
        Matcher m = p.matcher(text);

        return m.find() ? m.group() : "";
    }

    private String extractEmail(String text) {

        Pattern p = Pattern.compile(
                "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+"
        );

        Matcher m = p.matcher(text);

        return m.find() ? m.group() : "";
    }

    private String extractCity(String text) {

        if (text == null || text.trim().isEmpty()) return "";

        text = text.trim();

        Pattern p = Pattern.compile(
                "\\bcity\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)*)\\b",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(text);

    if (m.find()) return m.group(1).trim();

    // Known cities check
    String lower = text.toLowerCase();
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


        return "";
    }

    private String extractDeleteName(String text) {
        return text.replaceFirst("(?i)delete", "").trim();
    }

    private String extractUpdateName(String text) {

        Pattern p = Pattern.compile(
                "change\\s+(.+?)\\s+phone",
                Pattern.CASE_INSENSITIVE
        );

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

        } catch (Exception ignored) {
        }
    }
}