package com.aicrm.backend.service;

import com.aicrm.backend.dto.AuthRequest;
import com.aicrm.backend.dto.AuthResponse;
import com.aicrm.backend.model.User;
import com.aicrm.backend.repository.UserRepository;
import com.aicrm.backend.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ===============================
    // REGISTER
    // ===============================
    public AuthResponse register(AuthRequest request) {

        // Check email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encrypt password
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        // Default role USER
        if (request.getRole() == null ||
            request.getRole().isBlank()) {

            user.setRole("USER");

        } else {
            user.setRole(
                request.getRole().toUpperCase()
            );
        }

        userRepository.save(user);

        // Generate JWT
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

        // Password verify
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException(
                "Invalid password!"
            );
        }

        // Generate token
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