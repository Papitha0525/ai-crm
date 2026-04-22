package com.aicrm.backend.controller;

import com.aicrm.backend.model.Deal;
import com.aicrm.backend.service.DealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@CrossOrigin(origins = "*")
public class DealController {

    @Autowired
    private DealService dealService;

    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @PostMapping("/create")
    public Deal createDeal(@RequestBody Deal deal) {
        return dealService.createDeal(deal);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public List<Deal> getAllDeals() {
        return dealService.getAllDeals();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @GetMapping("/{id}")
    public Deal getDealById(@PathVariable Long id) {
        return dealService.getDealById(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SALESMAN')")
    @PutMapping("/{id}")
    public Deal updateDeal(@PathVariable Long id, @RequestBody Deal deal) {
        return dealService.updateDeal(id, deal);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteDeal(@PathVariable Long id) {
        return dealService.deleteDeal(id) ? "Deal deleted" : "Deal not found";
    }
}