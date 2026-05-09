package com.sysintegg7.potane.whisperwall.features.auth.service;

import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthRequest;
import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthResponse;
import com.sysintegg7.potane.whisperwall.features.auth.dto.RegisterRequest;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceAlreadyExistsException;
import com.sysintegg7.potane.whisperwall.shared.model.Role;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.shared.repository.RoleRepository;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import com.sysintegg7.potane.whisperwall.shared.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Locale;
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
        String normalizedUsername = normalizeRequiredValue(request.getUsername(), "Username");
        String normalizedEmail = normalizeRequiredValue(request.getEmail(), "Email").toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setIsVerified(false);
        user.setIsActive(true);

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("Username or email already exists");
        }

        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return new AuthResponse(token, "Bearer", savedUser.getId(), savedUser.getUsername(),
            savedUser.getEmail(), savedUser.getDisplayName(), resolvePrimaryRole(savedUser));
    }

    public AuthResponse login(AuthRequest request) {
        String loginIdentifier = resolveLoginIdentifier(request);

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginIdentifier, request.getPassword()));

        User user = userRepository.findByUsernameIgnoreCase(loginIdentifier)
            .or(() -> userRepository.findByEmailIgnoreCase(loginIdentifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(),
            user.getEmail(), user.getDisplayName(), resolvePrimaryRole(user));
    }

    private String resolveLoginIdentifier(AuthRequest request) {
        if (StringUtils.hasText(request.getUsername())) {
            return request.getUsername().trim();
        }
        if (StringUtils.hasText(request.getEmail())) {
            return request.getEmail().trim();
        }
        throw new RuntimeException("Username or email is required");
    }

    private String normalizeRequiredValue(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String resolvePrimaryRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "USER";
        }

        if (user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()))) {
            return "ADMIN";
        }

        if (user.getRoles().stream().anyMatch(role -> "MODERATOR".equals(role.getName()))) {
            return "MODERATOR";
        }

        return "USER";
    }
}
