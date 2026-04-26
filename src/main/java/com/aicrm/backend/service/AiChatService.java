package com.aicrm.backend.service;

import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    public String ask(String msg) {
        return "AI Response : " + msg;
    }
}