package com.sprintify.identityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprintify.identityservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
