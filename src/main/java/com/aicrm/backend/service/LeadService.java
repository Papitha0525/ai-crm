// ===============================
// LeadService.java
// Path:
// src/main/java/com/aicrm/backend/service/LeadService.java
// ===============================

package com.aicrm.backend.service;

import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.repository.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

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
        lead.setSource(dto.getSource());
        lead.setStatus("NEW");

        lead.setFollowUp(dto.getFollowUp());
    lead.setDealStatus(
        dto.getDealStatus() != null
        ? dto.getDealStatus() : "PENDING");

        return leadRepository.save(lead);
    }

    // ===============================
    // GET ALL LEADS
    // ===============================
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    // ===============================
    // GET LEAD COUNT
    // ===============================
    public long getLeadCount() {
        return leadRepository.count();
    }

    // ===============================
    // GET BY ID
    // ===============================
    public Lead getLeadById(Long id) {
        Optional<Lead> optional = leadRepository.findById(id);
        return optional.orElse(null);
    }

    // ===============================
    // FIND SINGLE BY NAME
    // ===============================
    public Lead findByName(String name) {
        return leadRepository.findByNameIgnoreCase(name);
    }

    // ===============================
    // FIND ALL SAME NAME LEADS
    // FIX FOR YOUR ERROR
    // ===============================
    public List<Lead> findAllByName(String name) {
        return leadRepository.findAllByNameIgnoreCase(name);
    }

    // ===============================
    // DELETE BY ID
    // ===============================
    public boolean deleteLead(Long id) {

        if (leadRepository.existsById(id)) {
            leadRepository.deleteById(id);
            return true;
        }

        return false;
    }

    // ===============================
    // DELETE BY NAME
    // ===============================
    public boolean deleteLeadByName(String name) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead != null) {
            leadRepository.delete(lead);
            return true;
        }

        return false;
    }

    // ===============================
    // UPDATE PHONE BY NAME
    // ===============================
    public boolean updatePhoneByName(String name, String phone) {

        Lead lead = leadRepository.findByNameIgnoreCase(name);

        if (lead != null) {
            lead.setPhone(phone);
            leadRepository.save(lead);
            return true;
        }

        return false;
    }

    // ===============================
    // UPDATE FULL LEAD
    // ===============================
    public Lead updateLead(Long id, LeadDto dto) {

        Optional<Lead> optional = leadRepository.findById(id);

        if (optional.isPresent()) {

            Lead lead = optional.get();

            lead.setName(dto.getName());
            lead.setPhone(dto.getPhone());
            lead.setEmail(dto.getEmail());
            lead.setCity(dto.getCity());
            lead.setRequirement(dto.getRequirement());
            lead.setSource(dto.getSource());

            return leadRepository.save(lead);
        }
        
        return null;
    }// Deal Status Update
public boolean updateDealStatus(String name,
                                 String status) {
    Lead lead = leadRepository
                    .findByNameIgnoreCase(name);
    if (lead == null) return false;
    lead.setDealStatus(status);
    leadRepository.save(lead);
    return true;
}

// Follow Up Update
public boolean updateFollowUp(String name,
                               String followUp) {
    Lead lead = leadRepository
                    .findByNameIgnoreCase(name);
    if (lead == null) return false;
    lead.setFollowUp(followUp);
    leadRepository.save(lead);
    return true;
}
// ID மூலம் எந்த field-உம் update பண்ணு
public boolean updateFieldById(Long id, 
                                String field, 
                                String value) {
    Optional<Lead> optional = 
        leadRepository.findById(id);
    if (optional.isEmpty()) return false;

    Lead lead = optional.get();

    switch (field) {
        case "name"       -> lead.setName(value);
        case "phone"      -> lead.setPhone(value);
        case "email"      -> lead.setEmail(value);
        case "city"       -> lead.setCity(value);
        case "dealStatus" -> lead.setDealStatus(value);
        case "followUp"   -> lead.setFollowUp(value);
        case "status"     -> lead.setStatus(value);
    }

    leadRepository.save(lead);
    return true;
}

}