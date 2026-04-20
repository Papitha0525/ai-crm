package com.aicrm.backend.service;

import com.aicrm.backend.model.Contact;
import com.aicrm.backend.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    public Contact updateContact(Long id, Contact contact) {
        Optional<Contact> existing = contactRepository.findById(id);
        if (existing.isPresent()) {
            Contact c = existing.get();
            c.setName(contact.getName());
            c.setPhone(contact.getPhone());
            c.setEmail(contact.getEmail());
            c.setCompany(contact.getCompany());
            return contactRepository.save(c);
        }
        return null;
    }

    public boolean deleteContact(Long id) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            return true;
        }
        return false;
    }
}