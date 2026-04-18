package com.sprintify.identityservice.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sprintify.identityservice.entity.Role;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if Admin already exists to avoid duplication
            if (userRepository.findByEmail("admin@sprintify.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@sprintify.com");
                // Password will be: admin123
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                
                userRepository.save(admin);
                
                System.out.println("---------------------------------------");
                System.out.println("SUCCESS: Admin account created!");
                System.out.println("Email: admin@sprintify.com");
                System.out.println("Password: admin123");
                System.out.println("---------------------------------------");
            } else {
                System.out.println("INFO: Admin account already exists.");
            }
        };
    }
}