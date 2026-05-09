package com.sprintify.identityservice.aop;

import com.sprintify.identityservice.dto.AuthResponseDTO;
import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;
import com.sprintify.identityservice.service.AsyncGamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AOP Aspect to handle gamification service calls asynchronously.
 * Intercepts login and signup methods to award XP and check streaks.
 * Similar to GenericAuditLoggingAspect pattern.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationAspect {

    private final AsyncGamificationService asyncGamificationService;
    private final UserRepository userRepository;

    /**
     * Intercepts signUp method after successful return and triggers async gamification
     */
    @AfterReturning(
            pointcut = "execution(* com.sprintify.identityservice.service.AuthService.signUp(..))",
            returning = "result"
    )
    public void afterSignup(JoinPoint joinPoint, Object result) {
        try {
            if (result instanceof AuthResponseDTO authResponse) {
                String email = authResponse.email();
                
                // Lookup user by email to get the UUID
                Optional<User> user = userRepository.findByEmail(email);
                if (user.isPresent()) {
                    var userId = user.get().getId();
                    
                    // Award signup XP and check streak asynchronously
                    asyncGamificationService.awardSignupXp(userId);
                    asyncGamificationService.checkStreak(userId);
                    
                    log.info("Gamification: triggered for signup - user: {}", email);
                } else {
                    log.warn("Could not find user for signup gamification: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("Error in GamificationAspect.afterSignup: {}", e.getMessage(), e);
        }
    }

    /**
     * Intercepts login method after successful return and triggers async gamification
     */
    @AfterReturning(
            pointcut = "execution(* com.sprintify.identityservice.service.AuthService.login(..))",
            returning = "result"
    )
    public void afterLogin(JoinPoint joinPoint, Object result) {
        try {
            if (result instanceof AuthResponseDTO authResponse) {
                String email = authResponse.email();
                
                // Lookup user by email to get the UUID
                Optional<User> user = userRepository.findByEmail(email);
                if (user.isPresent()) {
                    var userId = user.get().getId();
                    
                    // Award login XP and check streak asynchronously
                    asyncGamificationService.awardLoginXp(userId);
                    asyncGamificationService.checkStreak(userId);
                    
                    log.info("Gamification: triggered for login - user: {}", email);
                } else {
                    log.warn("Could not find user for login gamification: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("Error in GamificationAspect.afterLogin: {}", e.getMessage(), e);
        }
    }
}

