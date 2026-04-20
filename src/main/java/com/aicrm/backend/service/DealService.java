package com.aicrm.backend.service;

import com.aicrm.backend.model.Deal;
import com.aicrm.backend.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DealService {

    @Autowired
    private DealRepository dealRepository;

    public Deal createDeal(Deal deal) {
        return dealRepository.save(deal);
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public Deal getDealById(Long id) {
        return dealRepository.findById(id).orElse(null);
    }

    public Deal updateDeal(Long id, Deal deal) {
        Optional<Deal> existing = dealRepository.findById(id);
        if (existing.isPresent()) {
            Deal d = existing.get();
            d.setTitle(deal.getTitle());
            d.setValue(deal.getValue());
            d.setStage(deal.getStage());
            d.setCloseDate(deal.getCloseDate());
            return dealRepository.save(d);
        }
        return null;
    }

    public boolean deleteDeal(Long id) {
        if (dealRepository.existsById(id)) {
            dealRepository.deleteById(id);
            return true;
        }
        return false;
    }
}