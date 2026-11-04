package com.logistics.service;

import com.logistics.dto.AuthResponse;
import com.logistics.dto.LoginRequest;
import com.logistics.dto.RegisterRequest;
import com.logistics.model.User;
import com.logistics.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return AuthResponse.fail("Username and password are required.");
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername().trim());
        if (userOpt.isEmpty()) {
            return AuthResponse.fail("User not found.");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword().trim())) {
            return AuthResponse.fail("Invalid credentials.");
        }

        return AuthResponse.ok(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return AuthResponse.fail("Username and password cannot be empty.");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            return AuthResponse.fail("Username already exists.");
        }

        String role = "ADMIN".equalsIgnoreCase(request.getRole()) ? "ADMIN" : "USER";
        String fullName = request.getFullName() != null && !request.getFullName().trim().isEmpty() ?
                request.getFullName().trim() : username;

        User user = new User(username, request.getPassword().trim(), fullName, role, request.getEmail());
        user = userRepository.save(user);

        return AuthResponse.ok(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }
}
