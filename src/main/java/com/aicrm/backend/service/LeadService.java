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

    // ===============================
    // CREATE LEAD
    // ===============================
    public Lead createLead(LeadDto dto) {

        Lead lead = new Lead();

        lead.setName(dto.getName());
        lead.setPhone(dto.getPhone());
        lead.setEmail(dto.getEmail());
        lead.setCity(dto.getCity());
        lead.setRequirement(dto.getRequirement());
        lead.setSource(
                dto.getSource() != null &&
                !dto.getSource().isBlank()
                        ? dto.getSource()
                        : "MANUAL"
        );

        lead.setStatus("NEW");

        lead.setFollowUp(dto.getFollowUp());

        lead.setDealStatus(
                dto.getDealStatus() != null &&
                !dto.getDealStatus().isBlank()
                        ? dto.getDealStatus()
                        : "PENDING"
        );

        return leadRepository.save(lead);
    }

    // ===============================
    // GET ALL LEADS
    // ===============================
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    // ===============================
    // GET BY ID
    // ===============================
    public Lead getLeadById(Long id) {
        return leadRepository.findById(id).orElse(null);
    }

    // ===============================
    // COUNT ALL LEADS
    // ===============================
    public long getLeadCount() {
        return leadRepository.count();
    }

    // ===============================
    // FIND BY NAME
    // ===============================
    public Lead findByName(String name) {
        return leadRepository.findByNameIgnoreCase(name);
    }

    public List<Lead> findAllByName(String name) {
        return leadRepository.findAllByNameIgnoreCase(name);
    }

    public List<Lead> searchByName(String keyword) {
        return leadRepository.findByNameContainingIgnoreCase(keyword);
    }

    // ===============================
    // DELETE
    // ===============================
    public boolean deleteLead(Long id) {

        if (leadRepository.existsById(id)) {
            leadRepository.deleteById(id);
            return true;
        }

        return false;
    }

    public boolean deleteLeadByName(String name) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead != null) {
            leadRepository.delete(lead);
            return true;
        }

        return false;
    }

    // ===============================
    // UPDATE FULL LEAD
    // ===============================
    public Lead updateLead(Long id, LeadDto dto) {

        Optional<Lead> optional = leadRepository.findById(id);

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

        return leadRepository.save(lead);
    }

    // ===============================
    // UPDATE PHONE
    // ===============================
    public boolean updatePhoneByName(String name, String phone) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setPhone(phone);
        leadRepository.save(lead);

        return true;
    }

    // ===============================
    // DEAL STATUS
    // ===============================
    public boolean updateDealStatus(String name, String status) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setDealStatus(status);
        leadRepository.save(lead);

        return true;
    }

    // ===============================
    // FOLLOW UP
    // ===============================
    public boolean updateFollowUp(String name, String followUp) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead == null) {
            return false;
        }

        lead.setFollowUp(followUp);
        leadRepository.save(lead);

        return true;
    }

    // ===============================
    // UPDATE ANY FIELD
    // ===============================
    public boolean updateFieldById(
            Long id,
            String field,
            String value
    ) {

        Optional<Lead> optional =
                leadRepository.findById(id);

        if (optional.isEmpty()) {
            return false;
        }

        Lead lead = optional.get();

        switch (field) {
            case "name" -> lead.setName(value);
            case "phone" -> lead.setPhone(value);
            case "email" -> lead.setEmail(value);
            case "city" -> lead.setCity(value);
            case "status" -> lead.setStatus(value);
            case "dealStatus" -> lead.setDealStatus(value);
            case "followUp" -> lead.setFollowUp(value);
            case "requirement" -> lead.setRequirement(value);
        }

        leadRepository.save(lead);

        return true;
    }

    // ===============================
    // ASSIGN LEAD TO SALESMAN
    // ===============================
    public boolean assignLead(Long leadId, Long userId) {

        Optional<Lead> leadOpt =
                leadRepository.findById(leadId);

        Optional<User> userOpt =
                userRepository.findById(userId);

        if (leadOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Lead lead = leadOpt.get();
        lead.setAssignedTo(userOpt.get());

        leadRepository.save(lead);

        return true;
    }

    // ===============================
    // SALESMAN OWN LEADS
    // ===============================
    public List<Lead> getAssignedLeads(String email) {
        return leadRepository.findByAssignedToEmail(email);
    }

    public List<Lead> getAssignedLeadsByUserId(Long id) {
        return leadRepository.findByAssignedToId(id);
    }

    // ===============================
    // DASHBOARD COUNTS
    // ===============================
    public long getNewLeadsCount() {
        return leadRepository.countByStatus("NEW");
    }

    public long getWonDealsCount() {
        return leadRepository.countByDealStatus("WON");
    }

    public long getLostDealsCount() {
        return leadRepository.countByDealStatus("LOST");
    }

    public long getPendingDealsCount() {
        return leadRepository.countByDealStatus("PENDING");
    }

    // ===============================
    // REPORTS
    // ===============================
    public List<Lead> getWonLeads() {
        return leadRepository.findByDealStatus("WON");
    }

    public List<Lead> getLostLeads() {
        return leadRepository.findByDealStatus("LOST");
    }

    public List<Lead> getChatSourceLeads() {
        return leadRepository.findBySource("CHAT");
    }
}