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

    // Register
    public AuthResponse register(AuthRequest request) {
        // Email already exists check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // User create பண்ணு
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("SALES_REP");

        userRepository.save(user);

        // Token generate பண்ணு
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getName(),
                                user.getEmail(), user.getRole());
    }

    // Login
    public AuthResponse login(AuthRequest request) {
        // User find பண்ணு
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                    new RuntimeException("User not found!"));

        // Password check பண்ணு
        if (!passwordEncoder.matches(request.getPassword(),
                                     user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // Token generate பண்ணு
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getName(),
                                user.getEmail(), user.getRole());
    }
}