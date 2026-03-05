package com.sysintegg7.potane.whisperwall.service;

import com.sysintegg7.potane.whisperwall.dto.AuthRequest;
import com.sysintegg7.potane.whisperwall.dto.AuthResponse;
import com.sysintegg7.potane.whisperwall.dto.RegisterRequest;
import com.sysintegg7.potane.whisperwall.exception.ResourceAlreadyExistsException;
import com.sysintegg7.potane.whisperwall.model.Role;
import com.sysintegg7.potane.whisperwall.model.User;
import com.sysintegg7.potane.whisperwall.repository.RoleRepository;
import com.sysintegg7.potane.whisperwall.repository.UserRepository;
import com.sysintegg7.potane.whisperwall.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setIsVerified(false);
        user.setIsActive(true);

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return new AuthResponse(token, "Bearer", savedUser.getId(), savedUser.getUsername(),
                savedUser.getEmail(), savedUser.getDisplayName());
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(),
                user.getEmail(), user.getDisplayName());
    }
}
