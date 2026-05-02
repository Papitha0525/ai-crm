package com.aicrm.backend.service;

import com.aicrm.backend.dto.AuthRequest;
import com.aicrm.backend.dto.AuthResponse;
import com.aicrm.backend.model.User;
import com.aicrm.backend.repository.UserRepository;
import com.aicrm.backend.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ===============================
    // REGISTER
    // ===============================
    public AuthResponse register(AuthRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // ✅ Plain password
        user.setPassword(request.getPassword());

       user.setRole("USER");

        userRepository.save(user);

        String token = jwtUtil.generateToken(
            user.getEmail(),
            user.getRole()
        );

        return new AuthResponse(
            token,
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }

    // ===============================
    // LOGIN
    // ===============================
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(() ->
            new RuntimeException("User not found!")
        );

        // ✅ Plain password check
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtUtil.generateToken(
            user.getEmail(),
            user.getRole()
        );

        return new AuthResponse(
            token,
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}