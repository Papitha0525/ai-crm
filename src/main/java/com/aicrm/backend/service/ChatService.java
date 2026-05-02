package com.aicrm.backend.service;
 
import com.aicrm.backend.dto.ChatRequest;
import com.aicrm.backend.dto.ChatResponse;
import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.ChatHistory;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.model.User;
import com.aicrm.backend.repository.ChatHistoryRepository;
import com.aicrm.backend.repository.UserRepository;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
@Service
public class ChatService {
 
    @Autowired
    private LeadService leadService;
 
    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
 
    @Autowired(required = false)
    private AiOrchestratorService aiService;
 
    // ── Pending state ──────────────────────────────────────────────────────────
    private List<Lead> pendingDeleteList = new ArrayList<>();
    private LeadDto    pendingLead       = null;
 
    private boolean waitingLeadInput     = false;
    private boolean waitingSalesmanInput = false;
    private int     salesmanStep         = 0;
 
    private String tempName     = null;
    private String tempEmail    = null;
 
    // ==========================================================================
    // MAIN ENTRY
    // ==========================================================================
    public ChatResponse processMessage(ChatRequest request) {
 
        String originalMsg = request.getMessage().trim();
        String msg         = originalMsg.toLowerCase(Locale.ROOT).trim();
 
        String role = request.getRole();
        if (role == null) role = "";
        role = role.trim().toUpperCase();
 
        boolean isAdmin = role.equals("ADMIN");
 
        ChatResponse response;
 
        try {
 
            // ==================================================================
            // CANCEL
            // ==================================================================
            if (msg.matches("^(cancel|reset|stop|exit)$")) {
 
                waitingLeadInput     = false;
                waitingSalesmanInput = false;
                salesmanStep         = 0;
                tempName             = null;
                tempEmail            = null;
                pendingLead          = null;
                pendingDeleteList.clear();
 
                response = new ChatResponse(
                        "✅ Cancelled successfully.", "CANCELLED", null);
 
            // ==================================================================
            // GREETING
            // ==================================================================
            } else if (msg.matches("^(hi|hello|hey|hii)$")) {
 
                response = new ChatResponse(
                        "👋 Hello " + role + "\n\n"
                        + "Commands:\n"
                        + "• create lead Ravi 9876543210 ravi@gmail.com Chennai\n"
                        + "• show leads\n"
                        + "• show won deals\n"
                        + "• reports\n"
                        + "• assign lead Ravi to Raj",
                        "GREETING", null);
 
            // ==================================================================
            // WAITING – CREATE LEAD (multi-step)
            // ==================================================================
            } else if (waitingLeadInput) {
 
                fillMissingLeadFields(originalMsg);
                String missing = getMissingFields();
 
                if (!missing.isBlank()) {
 
                    response = new ChatResponse(
                            "Please provide:\n\n" + missing, "REQUIRED", null);
 
                } else {
 
                    pendingLead.setSource("CHAT");
                    pendingLead.setStatus("NEW");
                    pendingLead.setDealStatus("PENDING");
 
                    Lead lead = leadService.createLead(pendingLead);
 
                    waitingLeadInput = false;
                    pendingLead      = null;
 
                    response = new ChatResponse(
                            "✅ Lead created successfully.\n\n👤 " + lead.getName(),
                            "CREATED", lead);
                }
 
            // ==================================================================
            // WAITING – CREATE SALESMAN (multi-step)  ← must be BEFORE the
            //           "create salesman" trigger so follow-up inputs are caught
            // ==================================================================
            } else if (waitingSalesmanInput) {
 
                // STEP 1 → NAME
                if (salesmanStep == 1) {
 
                    String name = originalMsg.trim();
 
                    if (name.length() < 2 || !name.matches("[A-Za-z ]+")) {
 
                        response = new ChatResponse(
                                "❌ Invalid name.\n👉 Enter valid name:", "TEXT", null);
 
                    } else {
 
                        tempName     = name;
                        salesmanStep = 2;
 
                        response = new ChatResponse("👉 Enter Email:", "TEXT", null);
                    }
 
                // STEP 2 → EMAIL
                } else if (salesmanStep == 2) {
 
                    String email = originalMsg.trim();
 
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
 
                        response = new ChatResponse(
                                "❌ Invalid email.\n👉 Enter valid email:", "TEXT", null);
 
                    } else if (userRepository.existsByEmail(email)) {
 
                        response = new ChatResponse(
                                "❌ Email already exists.\n👉 Enter another email:",
                                "TEXT", null);
 
                    } else {
 
                        tempEmail    = email;
                        salesmanStep = 3;
 
                        response = new ChatResponse("👉 Enter Password:", "TEXT", null);
                    }
 
                // STEP 3 → PASSWORD
                } else if (salesmanStep == 3) {
 
                    String password = originalMsg.trim();
 
                    if (password.length() < 4) {
 
                        response = new ChatResponse(
                                "❌ Password too short.\n👉 Minimum 4 characters:",
                                "TEXT", null);
 
                    } else {
 
                        User user = new User();
                        user.setName(tempName);
                        user.setEmail(tempEmail);
                        user.setPassword(password);
                        user.setRole("SALESMAN");
 
                        userRepository.save(user);
 
                        // Reset state
                        waitingSalesmanInput = false;
                        salesmanStep         = 0;
                        tempName             = null;
                        tempEmail            = null;
 
                        response = new ChatResponse(
                                "✅ Salesman created successfully!", "CREATED", null);
                    }
 
                } else {
 
                    // Unexpected step – reset
                    waitingSalesmanInput = false;
                    salesmanStep         = 0;
 
                    response = new ChatResponse(
                            "❌ Something went wrong. Please try again.", "ERROR", null);
                }
 
            // ==================================================================
            // CREATE LEAD (single-shot or start multi-step)
            // ==================================================================
            } else if (msg.matches(".*(create|add|new).*lead.*")) {
 
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
                            "Need details:\n\n" + missing, "REQUIRED", null);
 
                } else {
 
                    pendingLead.setSource("CHAT");
                    pendingLead.setStatus("NEW");
                    pendingLead.setDealStatus("PENDING");
 
                    Lead lead = leadService.createLead(pendingLead);
                    pendingLead = null;
 
                    response = new ChatResponse(
                            "✅ Lead created successfully.\n\n👤 " + lead.getName(),
                            "CREATED", lead);
                }
 
            // ==================================================================
            // CREATE SALESMAN (start multi-step)
            // ==================================================================
            } else if (msg.matches(
                    ".*(create|add|new|register|make).*?(salesman|seller|employee).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can create salesman.", "DENIED", null);
 
                } else {
 
                    waitingSalesmanInput = true;
                    salesmanStep         = 1;
 
                    response = new ChatResponse(
                            "👤 Please provide:\nName", "TEXT", null);
                }
 
            // ==================================================================
            // PROMOTE USER → SALESMAN
            // ==================================================================
            } else if (msg.matches(".*(allow|add|make|give).*salesman.*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can add salesman.", "DENIED", null);
 
                } else {
 
                    response = promoteSalesman(originalMsg);
                }
 
            // ==================================================================
            // DEMOTE SALESMAN → USER
            // ==================================================================
            } else if (msg.matches(".*(delete|remove|revoke).*salesman.*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can remove salesman.", "DENIED", null);
 
                } else {
 
                    response = removeSalesman(originalMsg);
                }
 
            // ==================================================================
            // SALESMAN COUNT
            // ==================================================================
            } else if (msg.matches(
                    ".*(salesman count|how many salesman|total salesman).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can view salesman count.", "DENIED", null);
 
                } else {
 
                    long count = userRepository.countByRole("SALESMAN");
 
                    response = new ChatResponse(
                            "👨‍💼 Total Salesman : " + count, "COUNT", null);
                }
 
            // ==================================================================
            // USER COUNT
            // ==================================================================
            } else if (msg.matches(
                    ".*(user count|how many user|how many users|total user|total users).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can view user count.", "DENIED", null);
 
                } else {
 
                    long count = userRepository.countByRole("USER");
 
                    response = new ChatResponse(
                            "👤 Total Users : " + count, "COUNT", null);
                }
 
            // ==================================================================
            // SHOW SALESMAN LIST
            // ==================================================================
            } else if (msg.matches(
                    ".*(show salesman|salesman details|show all salesman).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can view salesman details.", "DENIED", null);
 
                } else {
 
                    response = showUsersByRole("SALESMAN", "👨‍💼 SALESMAN LIST");
                }
 
            // ==================================================================
            // SHOW USER LIST
            // ==================================================================
            } else if (msg.matches(
                    ".*(show users|user details|show all users).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can view user details.", "DENIED", null);
 
                } else {
 
                    response = showUsersByRole("USER", "👤 USER LIST");
                }
 
            // ==================================================================
            // ASSIGN LEAD
            // ==================================================================
            } else if (msg.matches(".*assign.*lead.*to.*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can assign leads.", "DENIED", null);
 
                } else {
 
                    response = assignLeadCommand(originalMsg);
                }
 
            // ==================================================================
            // DELETE LEAD
            // ==================================================================
            } else if (msg.startsWith("delete ")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can delete leads.", "DENIED", null);
 
                } else {
 
                    String name  = extractDeleteName(originalMsg);
                    List<Lead> leads = leadService.findAllByName(name);
 
                    if (leads.isEmpty()) {
 
                        response = new ChatResponse(
                                "❌ Lead not found : " + name, "NOT_FOUND", null);
 
                    } else if (leads.size() == 1) {
 
                        boolean deleted = leadService.deleteLead(leads.get(0).getId());
 
                        response = new ChatResponse(
                                deleted ? "🗑 Lead deleted successfully." : "❌ Delete failed.",
                                deleted ? "DELETED" : "FAILED",
                                null);
 
                    } else {
 
                        pendingDeleteList = leads;
 
                        StringBuilder sb = new StringBuilder("Multiple leads found:\n\n");
 
                        for (int i = 0; i < leads.size(); i++) {
                            sb.append(i + 1)
                              .append(". ")
                              .append(leads.get(i).getName())
                              .append(" - ")
                              .append(leads.get(i).getPhone())
                              .append("\n");
                        }
 
                        sb.append("\nReply number to delete.");
 
                        response = new ChatResponse(sb.toString(), "MULTIPLE", null);
                    }
                }
 
            // ==================================================================
            // DELETE – PICK NUMBER FROM MULTIPLE
            // ==================================================================
            } else if (msg.matches("\\d+") && !pendingDeleteList.isEmpty()) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can delete.", "DENIED", null);
 
                } else {
 
                    int no = Integer.parseInt(msg);
 
                    if (no >= 1 && no <= pendingDeleteList.size()) {
 
                        Lead    lead    = pendingDeleteList.get(no - 1);
                        boolean deleted = leadService.deleteLead(lead.getId());
 
                        pendingDeleteList.clear();
 
                        response = new ChatResponse(
                                deleted ? "🗑 Lead deleted." : "❌ Delete failed.",
                                deleted ? "DELETED" : "FAILED",
                                null);
 
                    } else {
 
                        response = new ChatResponse(
                                "❌ Invalid number.", "INVALID", null);
                    }
                }
 
            // ==================================================================
            // SHOW WON DEALS
            // ==================================================================
            } else if (msg.matches(".*(show won|won deals|won leads).*")) {
 
                List<Lead> leads = leadService.getWonLeads();
 
                response = buildLeadResponse(filterByRole(leads, request, role), role);
 
            // ==================================================================
            // SHOW LOST DEALS
            // ==================================================================
            } else if (msg.matches(".*(show lost|lost deals|lost leads).*")) {
 
                List<Lead> leads = leadService.getLostLeads();
 
                response = buildLeadResponse(filterByRole(leads, request, role), role);
 
            // ==================================================================
            // SHOW PENDING DEALS
            // ==================================================================
            } else if (msg.matches(".*(show pending|pending deals).*")) {
 
                List<Lead> leads = leadService.getPendingLeads();
 
                response = buildLeadResponse(filterByRole(leads, request, role), role);
 
            // ==================================================================
            // SHOW LEADS
            // ==================================================================
            } else if (msg.matches(".*(show|list|display|give).*lead.*")) {
 
                if (isAdmin) {
 
                    List<Lead> leads = leadService.getAllLeads();
                    response = buildLeadResponse(leads, role);
 
                } else if (role.equals("SALESMAN")) {
 
                    List<Lead> leads = leadService.getAssignedLeads(request.getUserId());
                    response = buildLeadResponse(leads, role);
 
                } else {
 
                    response = new ChatResponse(
                            "❌ USER cannot see all leads.", "DENIED", null);
                }
 
            // ==================================================================
            // REPORTS
            // ==================================================================
            } else if (msg.matches(
                    ".*(report|reports|crm report|sales report).*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can view reports.", "DENIED", null);
 
                } else {
 
                    response = generateAdminReport();
                }
 
            // ==================================================================
            // UPDATE LEAD
            // ==================================================================
            } else if (msg.matches(".*(update|change|modify).*")) {
 
                response = handleUpdateCommand(originalMsg, request, role);
 
            // ==================================================================
            // DEAL STATUS UPDATE
            // ==================================================================
            } else if (msg.matches(".*\\b(won|lost|pending)\\b.*")) {
 
                response = handleDealUpdate(originalMsg, request, role);
 
            // ==================================================================
            // PDF REPORT
            // ==================================================================
            } else if (msg.matches(".*(pdf|download pdf|export pdf).*report.*")) {
 
                if (!isAdmin) {
 
                    response = new ChatResponse(
                            "❌ Only ADMIN can download PDF report.", "DENIED", null);
 
                } else {
 
                    response = new ChatResponse(
                            "📄 PDF Report Generated Successfully.", "PDF", null);
                }
 
            // ==================================================================
            // AI FALLBACK
            // ==================================================================
            } else {
 
                String aiReply = "AI disabled";
 
                if (aiService != null) {
                    aiReply = aiService.askGroq(originalMsg);
                }
 
                response = new ChatResponse(aiReply, "AI_REPLY", null);
            }
 
            saveChatHistory(request, response.getReply());
            return response;
 
        } catch (Exception e) {
 
            e.printStackTrace();
 
            return new ChatResponse("Error : " + e.getMessage(), "ERROR", null);
        }
    }
 
    // ==========================================================================
    // PRIVATE HELPERS
    // ==========================================================================
 
    private String nvl(String v) {
        return v == null ? "-" : v;
    }
 
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse showUsersByRole(String role, String title) {
 
        List<User> users = userRepository.findByRole(role);
 
        if (users == null || users.isEmpty()) {
            return new ChatResponse("No records found.", "EMPTY", null);
        }
 
        StringBuilder sb = new StringBuilder(title).append("\n\n");
        int i = 1;
 
        for (User u : users) {
            sb.append("#").append(i++).append("\n");
            sb.append("👤 ").append(nvl(u.getName())).append("\n");
            sb.append("📧 ").append(nvl(u.getEmail())).append("\n");
            sb.append("🛡 ").append(nvl(u.getRole())).append("\n");
            sb.append("──────────────\n");
        }
 
        return new ChatResponse(sb.toString(), "SHOW", null);
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse promoteSalesman(String text) {
 
        String email = extractEmail(text);
 
        Optional<User> optional = userRepository.findByEmailIgnoreCase(email);
 
        if (optional.isEmpty()) {
            return new ChatResponse("❌ User not found.", "NOT_FOUND", null);
        }
 
        User user = optional.get();
        user.setRole("SALESMAN");
        userRepository.save(user);
 
        return new ChatResponse(
                "✅ " + email + " promoted as SALESMAN.", "UPDATED", null);
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse removeSalesman(String text) {
 
        String name = extractLastWord(text);
 
        Optional<User> optional = userRepository.findByNameIgnoreCase(name);
 
        if (optional.isEmpty()) {
            return new ChatResponse("❌ Salesman not found.", "NOT_FOUND", null);
        }
 
        User user = optional.get();
        user.setRole("USER");
        userRepository.save(user);
 
        return new ChatResponse(
                "✅ " + user.getName() + " changed to USER.", "UPDATED", null);
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse assignLeadCommand(String text) {
 
        boolean ok = leadService.assignLeadToSalesmanName(text);
 
        return new ChatResponse(
                ok ? "✅ Lead assigned successfully." : "❌ Assignment failed.",
                ok ? "UPDATED" : "FAILED",
                null);
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse generateAdminReport() {
 
        long total   = leadService.getLeadCount();
        long won     = leadService.getWonDealsCount();
        long lost    = leadService.getLostDealsCount();
        long pending = leadService.getPendingDealsCount();
 
        String report = "📊 CRM REPORT\n\n"
                + "👥 Total Leads : " + total  + "\n"
                + "🏆 Won Deals : "   + won     + "\n"
                + "❌ Lost Deals : "  + lost    + "\n"
                + "🕒 Pending : "     + pending;
 
        return new ChatResponse(report, "REPORT", null);
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse buildLeadResponse(List<Lead> leads, String role) {
 
        if (leads == null || leads.isEmpty()) {
            return new ChatResponse("No leads found.", "EMPTY", null);
        }
 
        StringBuilder sb = new StringBuilder();
        sb.append("📋 Leads : ").append(leads.size()).append("\n\n");
 
        int i = 1;
 
        for (Lead l : leads) {
 
            sb.append("#").append(i++).append("\n");
            sb.append("👤 ").append(nvl(l.getName())).append("\n");
 
            if (!role.equals("USER")) {
                sb.append("📞 ").append(nvl(l.getPhone())).append("\n");
                sb.append("📧 ").append(nvl(l.getEmail())).append("\n");
            }
 
            sb.append("🏙 ").append(nvl(l.getCity())).append("\n");
            sb.append("💼 ").append(nvl(l.getDealStatus())).append("\n");
            sb.append("──────────────\n");
        }
 
        return new ChatResponse(sb.toString(), "SHOW", leads);
    }
 
    // --------------------------------------------------------------------------
    private List<Lead> filterByRole(
            List<Lead> leads, ChatRequest request, String role) {
 
        if (role.equals("ADMIN"))    return leads;
 
        if (role.equals("SALESMAN"))
            return leadService.getAssignedLeads(request.getUserId());
 
        return new ArrayList<>();
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse handleUpdateCommand(
            String text, ChatRequest request, String role) {
 
        try {
 
            List<Lead> leads = role.equals("ADMIN")
                    ? leadService.getAllLeads()
                    : leadService.getAssignedLeads(request.getUserId());
 
            Lead target = findLeadFromText(text, leads);
 
            if (target == null) {
                return new ChatResponse("❌ Lead not found.", "NOT_FOUND", null);
            }
 
            String lower = text.toLowerCase();
 
            if (lower.contains("phone")) {
                leadService.updateFieldById(target.getId(), "phone", extractPhone(text));
                return new ChatResponse("✅ Phone updated.", "UPDATED", null);
            }
 
            if (lower.contains("email")) {
                leadService.updateFieldById(target.getId(), "email", extractEmail(text));
                return new ChatResponse("✅ Email updated.", "UPDATED", null);
            }
 
            if (lower.contains("city")) {
                leadService.updateFieldById(target.getId(), "city", extractCity(text));
                return new ChatResponse("✅ City updated.", "UPDATED", null);
            }
 
            return new ChatResponse("❌ Unknown update field.", "INVALID", null);
 
        } catch (Exception e) {
            return new ChatResponse("❌ Update failed.", "ERROR", null);
        }
    }
 
    // --------------------------------------------------------------------------
    private ChatResponse handleDealUpdate(
            String text, ChatRequest request, String role) {
 
        if (!role.equals("ADMIN")) {
            return new ChatResponse(
                    "❌ Only ADMIN can change deal status.", "DENIED", null);
        }
 
        try {
 
            List<Lead> leads = leadService.getAllLeads();
            Lead target = findLeadFromText(text, leads);
 
            if (target == null) {
                return new ChatResponse("❌ Lead not found.", "NOT_FOUND", null);
            }
 
            String lower  = text.toLowerCase();
            String status = "PENDING";
 
            if (lower.contains("won") || lower.contains("win")) {
                status = "WON";
            } else if (lower.contains("lost") || lower.contains("loss")) {
                status = "LOST";
            }
 
            leadService.updateFieldById(target.getId(), "dealStatus", status);
 
            return new ChatResponse("✅ Deal updated to " + status, "UPDATED", null);
 
        } catch (Exception e) {
            return new ChatResponse("❌ Deal update failed.", "ERROR", null);
        }
    }
 
    // --------------------------------------------------------------------------
    /** Find lead by #N index or by name substring. */
    private Lead findLeadFromText(String text, List<Lead> leads) {
 
        Matcher m = Pattern.compile("#(\\d+)").matcher(text);
 
        if (m.find()) {
            int no = Integer.parseInt(m.group(1));
            if (no >= 1 && no <= leads.size()) {
                return leads.get(no - 1);
            }
        }
 
        for (Lead l : leads) {
            if (text.toLowerCase().contains(l.getName().toLowerCase())) {
                return l;
            }
        }
 
        return null;
    }
 
    // --------------------------------------------------------------------------
    private void saveChatHistory(ChatRequest request, String reply) {
 
        try {
            ChatHistory h = new ChatHistory();
            h.setUserId(Long.parseLong(request.getUserId()));
            h.setSessionId(request.getSessionId());
            h.setMessage(request.getMessage());
            h.setResponse(reply);
            chatHistoryRepository.save(h);
        } catch (Exception ignored) {
        }
    }
 
    // ==========================================================================
    // LEAD FIELD HELPERS
    // ==========================================================================
 
    private String getMissingFields() {
 
        StringBuilder sb = new StringBuilder();
 
        if (isBlank(pendingLead.getName()))       sb.append("• Name\n");
        if (isBlank(pendingLead.getPhone()))      sb.append("• Phone\n");
        if (isBlank(pendingLead.getEmail()))      sb.append("• Email\n");
        if (isBlank(pendingLead.getCity()))       sb.append("• City\n");
        if (isBlank(pendingLead.getRequirement())) sb.append("• Requirement\n");
 
        return sb.toString();
    }
 
    private void fillMissingLeadFields(String text) {
 
        if (isBlank(pendingLead.getName()))        pendingLead.setName(extractName(text));
        if (isBlank(pendingLead.getPhone()))       pendingLead.setPhone(extractPhone(text));
        if (isBlank(pendingLead.getEmail()))       pendingLead.setEmail(extractEmail(text));
        if (isBlank(pendingLead.getCity()))        pendingLead.setCity(extractCity(text));
        if (isBlank(pendingLead.getRequirement())) pendingLead.setRequirement(text.trim());
    }
 
    // --------------------------------------------------------------------------
    private String extractName(String text) {
 
        String cleaned = text
                .replaceAll("(?i)create|add|new|lead|for", "")
                .replaceAll("\\b\\d{10}\\b", "")
                .replaceAll("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+", "")
                .replaceAll("(?i)chennai|salem|madurai|trichy|coimbatore|erode|bangalore", "")
                .replaceAll("\\s+", " ")
                .trim();
 
        return cleaned;
    }
 
    private String extractPhone(String text) {
        Matcher m = Pattern.compile("\\d{10}").matcher(text);
        return m.find() ? m.group() : "";
    }
 
    private String extractEmail(String text) {
        Matcher m = Pattern.compile("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+").matcher(text);
        return m.find() ? m.group() : "";
    }
 
    private String extractCity(String text) {
 
        String[] cities = {
            "chennai", "madurai", "salem", "coimbatore",
            "erode", "trichy", "bangalore"
        };
 
        for (String city : cities) {
            if (text.toLowerCase().contains(city)) {
                return Character.toUpperCase(city.charAt(0)) + city.substring(1);
            }
        }
 
        return "";
    }
 
    private String extractRequirement(String text) {
 
        String lower = text.toLowerCase();
 
        if (lower.contains("website"))   return "Website";
        if (lower.contains("app"))       return "Mobile App";
        if (lower.contains("crm"))       return "CRM";
        if (lower.contains("software"))  return "Software";
        if (lower.contains("erp"))       return "ERP";
        if (lower.contains("billing"))   return "Billing Software";
        if (lower.contains("marketing")) return "Digital Marketing";
 
        String cleaned = text
                .replaceAll("(?i)create|add|new|lead|for", "")
                .replaceAll("\\b\\d{10}\\b", "")
                .replaceAll("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+", "")
                .replaceAll("(?i)chennai|salem|madurai|trichy|coimbatore|erode|bangalore", "")
                .trim();
 
        String[] parts = cleaned.split("\\s+");
 
        if (parts.length > 2) {
            StringBuilder req = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                req.append(parts[i]).append(" ");
            }
            return req.toString().trim();
        }
 
        return "";
    }
 
    private String extractLastWord(String text) {
        String[] arr = text.trim().split("\\s+");
        return arr.length == 0 ? "" : arr[arr.length - 1];
    }
 
    private String extractDeleteName(String text) {
        return text.replaceFirst("(?i)delete", "").trim();
    }
}