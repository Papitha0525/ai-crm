package com.aicrm.backend.controller;

import com.aicrm.backend.dto.LeadDto;
import com.aicrm.backend.model.Lead;
import com.aicrm.backend.service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    @Autowired
    private LeadService leadService;

    // CREATE
    @PostMapping("/create")
    public Lead createLead(@RequestBody LeadDto dto) {
        return leadService.createLead(dto);
    }

    // GET ALL
    @GetMapping("/all")
    public List<Lead> getAllLeads() {
        return leadService.getAllLeads();
    }

    // COUNT
    @GetMapping("/count")
    public long getLeadCount() {
        return leadService.getLeadCount();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Lead getLeadById(@PathVariable Long id) {
        return leadService.getLeadById(id);
    }

    // DELETE BY ID
    @DeleteMapping("/{id}")
    public String deleteLead(@PathVariable Long id) {
        return leadService.deleteLead(id)
                ? "Lead deleted"
                : "Lead not found";
    }

    // DELETE BY NAME
    @DeleteMapping("/delete/{name}")
    public String deleteLeadByName(@PathVariable String name) {
        return leadService.deleteLeadByName(name)
                ? "Lead deleted"
                : "Lead not found";
    }

    // UPDATE
    @PutMapping("/{id}")
    public Lead updateLead(@PathVariable Long id,
                           @RequestBody LeadDto dto) {
        return leadService.updateLead(id, dto);
    }
}