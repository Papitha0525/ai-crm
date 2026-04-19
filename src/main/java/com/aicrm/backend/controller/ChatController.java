package com.aicrm.backend.controller;

import com.aicrm.backend.dto.ChatRequest;
import com.aicrm.backend.dto.ChatResponse;
import com.aicrm.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Chat message send பண்ணு
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
            chatService.processMessage(request));
    }
}