package com.aicrm.backend.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LeadParserService {

    // Extract Name
    public String extractName(String text) {

        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        Pattern p1 = Pattern.compile(
                "(?i)(?:create\\s+a?\\s*lead\\s+for|lead\\s+for|for)\\s+([A-Za-z]+)"
        );

        Matcher m1 = p1.matcher(text);

        if (m1.find()) {
            return m1.group(1).trim();
        }

        Pattern p2 = Pattern.compile("(?i)name\\s+([A-Za-z]+)");

        Matcher m2 = p2.matcher(text);

        if (m2.find()) {
            return m2.group(1).trim();
        }

        return "";
    }

    // Extract Phone
    public String extractPhone(String text) {

        Pattern p = Pattern.compile("\\b\\d{10}\\b");
        Matcher m = p.matcher(text);

        return m.find() ? m.group() : "";
    }

    // Extract Email
    public String extractEmail(String text) {

        Pattern p = Pattern.compile(
                "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        );

        Matcher m = p.matcher(text);

        return m.find() ? m.group() : "";
    }

    // Extract Requirement
    public String extractRequirement(String text) {

        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        Pattern p = Pattern.compile(
                "(?i)(looking for|need|needs|requires)\\s+(.+?)(?:\\s+in\\s+[A-Za-z ]+|$)"
        );

        Matcher m = p.matcher(text);

        if (m.find()) {
            return m.group(2).trim();
        }

        return "";
    }

    // Extract City
    public String extractCity(String text) {

        if (text == null || text.trim().isEmpty()) {
            return "";
        }

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
        if (lower.contains("salem")) return "Salem";
        if (lower.contains("trichy")) return "Trichy";
        if (lower.contains("erode")) return "Erode";

        Pattern p = Pattern.compile(
                "(?i)in\\s+([A-Za-z ]+)$"
        );

        Matcher m = p.matcher(text);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "";
    }

    // Extract Delete Name
    public String extractDeleteName(String text) {

        if (text == null) return "";

        text = text.trim();

        if (text.toLowerCase().startsWith("delete")) {
            return text.substring(6).trim();
        }

        if (text.toLowerCase().endsWith("delete")) {
            return text.substring(0, text.length() - 6).trim();
        }

        return text.replaceAll("(?i)delete", "").trim();
    }

    // Extract Update Name
    public String extractUpdateName(String text) {

        Pattern p = Pattern.compile(
                "change\\s+(.+?)\\s+phone",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(text);

        return m.find() ? m.group(1).trim() : "";
    }

    // Validate Email
    public boolean isValidEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Pattern p = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

        return p.matcher(email.trim()).matches();
    }

    // Validate Phone
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }
}