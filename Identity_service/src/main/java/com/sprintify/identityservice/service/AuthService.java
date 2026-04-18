package com.sprintify.identityservice.service;

import com.sprintify.identityservice.dto.AuthResponseDTO;
import com.sprintify.identityservice.dto.LoginRequestDTO;
import com.sprintify.identityservice.dto.SignUpRequestDTO;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;
import com.sprintify.identityservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        //normalize the email
        String normalizedEmail = normalizeEmail(request.email());

        //check if the email is already taken
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole());

        return new AuthResponseDTO(token, savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponseDTO(token, user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }
}
