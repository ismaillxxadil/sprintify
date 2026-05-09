package com.sprintify.gamification_service.repository;

import com.sprintify.gamification_service.Entity.GamificationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, Long> {
    
    // دالة مخصصة للبحث عن تقدم اللاعب باستخدام الـ ID الخاص به
    Optional<GamificationProfile> findByUserId(String userId);
}