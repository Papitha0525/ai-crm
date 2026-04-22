package com.aicrm.backend.controller;

import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.service.LeadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    @Autowired
    private LeadService leadService;

    // CREATE
    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @PostMapping("/create")
    public Lead createLead(@RequestBody LeadDto dto) {
        return leadService.createLead(dto);
    }

    // ALL LEADS
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public List<Lead> getAllLeads() {
        return leadService.getAllLeads();
    }

    // COUNT
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/count")
    public long getLeadCount() {
        return leadService.getLeadCount();
    }

    // GET BY ID
    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN','USER')")
    @GetMapping("/{id}")
    public Lead getLeadById(@PathVariable Long id) {
        return leadService.getLeadById(id);
    }

    // DELETE BY ID
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteLead(@PathVariable Long id) {
        return leadService.deleteLead(id)
                ? "Lead deleted"
                : "Lead not found";
    }

    // DELETE BY NAME
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{name}")
    public String deleteLeadByName(@PathVariable String name) {
        return leadService.deleteLeadByName(name)
                ? "Lead deleted"
                : "Lead not found";
    }

    // UPDATE
    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @PutMapping("/{id}")
    public Lead updateLead(@PathVariable Long id,
                           @RequestBody LeadDto dto) {
        return leadService.updateLead(id, dto);
    }

    // ASSIGN LEAD TO SALESMAN
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/assign/{leadId}/{userId}")
    public String assignLead(@PathVariable Long leadId,
                             @PathVariable Long userId) {

        return leadService.assignLead(leadId, userId)
                ? "Lead assigned successfully"
                : "Lead or user not found";
    }

    // MY LEADS (SALESMAN)
    @PreAuthorize("hasAuthority('SALESMAN')")
    @GetMapping("/my")
    public List<Lead> myLeads(Authentication auth) {
        return leadService.getAssignedLeads(auth.getName());
    }

    // SEARCH
    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @GetMapping("/search/{keyword}")
    public List<Lead> search(@PathVariable String keyword) {
        return leadService.searchByName(keyword);
    }

    // DASHBOARD COUNTS
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public Map<String, Long> dashboard() {

        Map<String, Long> data = new HashMap<>();

        data.put("totalLeads",
                leadService.getLeadCount());

        data.put("newLeads",
                leadService.getNewLeadsCount());

        data.put("wonDeals",
                leadService.getWonDealsCount());

        data.put("lostDeals",
                leadService.getLostDealsCount());

        data.put("pendingDeals",
                leadService.getPendingDealsCount());

        return data;
    }
}