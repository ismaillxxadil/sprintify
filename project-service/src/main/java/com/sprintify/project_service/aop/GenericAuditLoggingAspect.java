package com.sprintify.project_service.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintify.project_service.dto.AuditLogRequestDTO;
import com.sprintify.project_service.service.AsyncAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GenericAuditLoggingAspect {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String UNKNOWN_USER_ID = "UNKNOWN_USER";

    private final AsyncAuditLogService asyncAuditLogService;
    private final ObjectMapper objectMapper;

    // Intercept ALL methods in the service package, EXCEPT the AsyncAuditLogService itself
    @AfterReturning(
            pointcut = "execution(* com.sprintify.project_service.service.*.*(..)) " +
                       "&& !execution(* com.sprintify.project_service.service.AsyncAuditLogService.*(..))",
            returning = "result"
    )
    public void logEveryAction(JoinPoint joinPoint, Object result) {

        // 1. Get the Method Name and Class Name dynamically
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 2. OPTIONAL: Skip read-only methods so you only log actions (Writes/Updates/Deletes)
        if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("load")) {
            return; 
        }

        try {
            String actorId = resolveActorId();

            // 3. Try to convert the first argument into a JSON map
            Object[] args = joinPoint.getArgs();
            Map<String, Object> detailsMap = null;
            if (args != null && args.length > 0 && args[0] != null) {
                // If it's a primitive (like a Long ID), wrap it. If it's a DTO, map it.
                detailsMap = args[0].getClass().isPrimitive() || args[0] instanceof String || args[0] instanceof Number
                        ? Map.of("requestData", args[0]) 
                        : objectMapper.convertValue(args[0], Map.class);
            }

            // 4. Try to extract an ID from the result if possible
            String entityId = extractIdFromResult(result);

            // 5. Build the log using the Class and Method names
            AuditLogRequestDTO logRequest = AuditLogRequestDTO.builder()
                    .actorId(actorId)
                    .actionType(methodName)     // e.g., "createSprint"
                    .entityType(className)      // e.g., "SprintService"
                    .entityId(entityId)
                    .details(detailsMap)
                    .build();

            // 6. Send to the log service asynchronously
            asyncAuditLogService.sendLogAsynchronously(logRequest);

        } catch (Exception e) {
            log.error("Failed to process generic audit log: {}", e.getMessage());
        }
    }

    private String resolveActorId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return UNKNOWN_USER_ID;
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String actorId = request.getHeader(USER_ID_HEADER);
        return actorId == null || actorId.isBlank() ? UNKNOWN_USER_ID : actorId;
    }

    // Helper method to try to pull the ID from Response DTOs
    private String extractIdFromResult(Object result) {
        if (result == null) return "UNKNOWN_ID";
        try {
            return result.getClass().getMethod("getId").invoke(result).toString();
        } catch (Exception e) {
            return "N/A";
        }
    }
}