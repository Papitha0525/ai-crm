package com.aicrm.backend.service;

import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.model.User;
import com.aicrm.backend.repository.LeadRepository;
import com.aicrm.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private UserRepository userRepository;

    // =====================================
    // CREATE LEAD
    // =====================================
    public Lead createLead(LeadDto dto) {

        Lead lead = new Lead();

        lead.setName(dto.getName());
        lead.setPhone(dto.getPhone());
        lead.setEmail(dto.getEmail());
        lead.setCity(dto.getCity());
        lead.setRequirement(dto.getRequirement());

        lead.setSource(
                isBlank(dto.getSource())
                        ? "MANUAL"
                        : dto.getSource()
        );

        lead.setStatus(
                isBlank(dto.getStatus())
                        ? "NEW"
                        : dto.getStatus()
        );

        lead.setDealStatus(
                isBlank(dto.getDealStatus())
                        ? "PENDING"
                        : dto.getDealStatus()
        );

        lead.setFollowUp(dto.getFollowUp());

        return leadRepository.save(lead);
    }

    // =====================================
    // GET ALL
    // =====================================
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElse(null);
    }

    public long getLeadCount() {
        return leadRepository.count();
    }

    // =====================================
    // FIND
    // =====================================
    public Lead findByName(String name) {
        return leadRepository
                .findByNameIgnoreCase(name);
    }

    public List<Lead> findAllByName(String name) {
        return leadRepository
                .findAllByNameIgnoreCase(name);
    }

    public List<Lead> searchByName(String keyword) {
        return leadRepository
                .findByNameContainingIgnoreCase(
                        keyword);
    }

    // =====================================
    // DELETE
    // =====================================
    public boolean deleteLead(Long id) {

        if (!leadRepository.existsById(id)) {
            return false;
        }

        leadRepository.deleteById(id);
        return true;
    }

    public boolean deleteLeadByName(String name) {

        Lead lead =
                leadRepository
                .findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        leadRepository.delete(lead);
        return true;
    }

    // =====================================
    // UPDATE FULL
    // =====================================
    public Lead updateLead(
            Long id,
            LeadDto dto) {

        Optional<Lead> optional =
                leadRepository.findById(id);

        if (optional.isEmpty()) {
            return null;
        }

        Lead lead = optional.get();

        lead.setName(dto.getName());
        lead.setPhone(dto.getPhone());
        lead.setEmail(dto.getEmail());
        lead.setCity(dto.getCity());
        lead.setRequirement(dto.getRequirement());
        lead.setSource(dto.getSource());
        lead.setStatus(dto.getStatus());
        lead.setDealStatus(dto.getDealStatus());
        lead.setFollowUp(dto.getFollowUp());

        return leadRepository.save(lead);
    }

    // =====================================
    // UPDATE FIELD
    // =====================================
    public boolean updateFieldById(
            Long id,
            String field,
            String value) {

        Optional<Lead> optional =
                leadRepository.findById(id);

        if (optional.isEmpty()) {
            return false;
        }

        Lead lead = optional.get();

        switch (field.toLowerCase()) {

            case "name" ->
                    lead.setName(value);

            case "phone" ->
                    lead.setPhone(value);

            case "email" ->
                    lead.setEmail(value);

            case "city" ->
                    lead.setCity(value);

            case "status" ->
                    lead.setStatus(value);

            case "dealstatus" ->
                    lead.setDealStatus(value);

            case "followup" ->
                    lead.setFollowUp(value);

            case "requirement" ->
                    lead.setRequirement(value);

            default ->
            {
                return false;
            }
        }

        leadRepository.save(lead);
        return true;
    }

    // =====================================
    // SIMPLE UPDATES
    // =====================================
    public boolean updatePhoneByName(
            String name,
            String phone) {

        Lead lead =
                leadRepository
                .findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setPhone(phone);
        leadRepository.save(lead);

        return true;
    }

    public boolean updateDealStatus(
            String name,
            String status) {

        Lead lead =
                leadRepository
                .findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setDealStatus(status);
        leadRepository.save(lead);

        return true;
    }

    public boolean updateFollowUp(
            String name,
            String note) {

        Lead lead =
                leadRepository
                .findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setFollowUp(note);
        leadRepository.save(lead);

        return true;
    }

    // =====================================
    // ASSIGN LEAD
    // =====================================
    public boolean assignLead(
            Long leadId,
            Long userId) {

        Optional<Lead> leadOpt =
                leadRepository.findById(leadId);

        Optional<User> userOpt =
                userRepository.findById(userId);

        if (leadOpt.isEmpty()
                || userOpt.isEmpty()) {
            return false;
        }

        Lead lead = leadOpt.get();
        User user = userOpt.get();

        lead.setAssignedTo(user);

        leadRepository.save(lead);
        return true;
    }

    // assign lead #2 to Ravi
    public boolean assignLeadToSalesmanName(
            String command) {

        try {

            Long leadId = null;

            java.util.regex.Matcher num =
                    java.util.regex.Pattern
                    .compile("#(\\d+)")
                    .matcher(command);

            if (num.find()) {

                int index =
                        Integer.parseInt(
                                num.group(1));

                List<Lead> all =
                        getAllLeads();

                if (index >= 1
                        && index <= all.size()) {

                    leadId =
                            all.get(index - 1)
                            .getId();
                }
            }

            if (leadId == null) {

                for (Lead l : getAllLeads()) {

                    if (command.toLowerCase()
                            .contains(
                             l.getName()
                             .toLowerCase())) {

                        leadId = l.getId();
                        break;
                    }
                }
            }

            if (leadId == null) {
                return false;
            }

            String[] arr =
                    command.trim()
                    .split("\\s+");

            String salesmanName =
                    arr[arr.length - 1];

            Optional<User> userOpt =
                    userRepository
                    .findByNameIgnoreCaseAndRole(
                            salesmanName,
                            "SALESMAN");

            if (userOpt.isEmpty()) {
                return false;
            }

            return assignLead(
                    leadId,
                    userOpt.get().getId()
            );

        } catch (Exception e) {
            return false;
        }
    }

    // =====================================
    // ASSIGNED LEADS
    // =====================================
    public List<Lead> getAssignedLeads(
            String email) {

        return leadRepository
                .findByAssignedToEmail(email);
    }

    public List<Lead> getAssignedLeadsByUserId(
            Long id) {

        return leadRepository
                .findByAssignedToId(id);
    }

    // =====================================
    // COUNTS
    // =====================================
    public long getNewLeadsCount() {
        return leadRepository
                .countByStatus("NEW");
    }

    public long getWonDealsCount() {
        return leadRepository
                .countByDealStatus("WON");
    }

    public long getLostDealsCount() {
        return leadRepository
                .countByDealStatus("LOST");
    }

    public long getPendingDealsCount() {
        return leadRepository
                .countByDealStatus("PENDING");
    }

    // =====================================
    // REPORTS / FILTERS
    // =====================================
    public List<Lead> getWonLeads() {
        return leadRepository
                .findByDealStatus("WON");
    }

    public List<Lead> getLostLeads() {
        return leadRepository
                .findByDealStatus("LOST");
    }

    public List<Lead> getPendingLeads() {
        return leadRepository
                .findByDealStatus("PENDING");
    }

    public List<Lead> getChatSourceLeads() {
        return leadRepository
                .findBySource("CHAT");
    }

    // =====================================
    // HELPERS
    // =====================================
    private boolean isBlank(String v) {
        return v == null
                || v.trim().isEmpty();
    }
}