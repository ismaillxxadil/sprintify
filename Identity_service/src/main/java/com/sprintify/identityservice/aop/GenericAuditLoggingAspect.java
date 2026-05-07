package com.sprintify.identityservice.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintify.identityservice.dto.AuditLogRequestDTO;
import com.sprintify.identityservice.service.AsyncAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GenericAuditLoggingAspect {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String UNKNOWN_USER_ID = "UNKNOWN_USER";
    private static final String REDACTED_VALUE = "***REDACTED***";
    private static final Set<String> SENSITIVE_FIELD_KEYS = Set.of(
            "password",
            "token",
            "secret",
            "credential",
            "authorization"
    );

    private final AsyncAuditLogService asyncAuditLogService;
    private final ObjectMapper objectMapper;

    @AfterReturning(
            pointcut = "execution(* com.sprintify.identityservice.service.*.*(..)) " +
                       "&& !execution(* com.sprintify.identityservice.service.AsyncAuditLogService.*(..))",
            returning = "result"
    )
    public void logEveryAction(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("load")) {
            return;
        }

        try {
            String actorId = resolveActorId();

            Object[] args = joinPoint.getArgs();
            Map<String, Object> detailsMap = null;
            if (args != null && args.length > 0 && args[0] != null) {
                detailsMap = args[0].getClass().isPrimitive() || args[0] instanceof String || args[0] instanceof Number
                        ? Map.of("requestData", args[0])
                        : objectMapper.convertValue(args[0], Map.class);
            }
            detailsMap = redactSensitiveData(detailsMap);

            String entityId = extractIdFromResult(result);

            AuditLogRequestDTO logRequest = AuditLogRequestDTO.builder()
                    .actorId(actorId)
                    .actionType(methodName)
                    .entityType(className)
                    .entityId(entityId)
                    .details(detailsMap)
                    .build();

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

    private String extractIdFromResult(Object result) {
        if (result == null) {
            return "UNKNOWN_ID";
        }

        try {
            return result.getClass().getMethod("getId").invoke(result).toString();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private Map<String, Object> redactSensitiveData(Map<String, Object> detailsMap) {
        if (detailsMap == null || detailsMap.isEmpty()) {
            return detailsMap;
        }

        Map<String, Object> sanitizedDetails = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : detailsMap.entrySet()) {
            if (isSensitiveField(entry.getKey())) {
                sanitizedDetails.put(entry.getKey(), REDACTED_VALUE);
                continue;
            }
            sanitizedDetails.put(entry.getKey(), sanitizeValue(entry.getValue()));
        }
        return sanitizedDetails;
    }

    private Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Object> sanitizedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                String key = String.valueOf(nestedEntry.getKey());
                if (isSensitiveField(key)) {
                    sanitizedMap.put(key, REDACTED_VALUE);
                    continue;
                }
                sanitizedMap.put(key, sanitizeValue(nestedEntry.getValue()));
            }
            return sanitizedMap;
        }
        if (value instanceof List<?> listValue) {
            List<Object> sanitizedList = new ArrayList<>(listValue.size());
            for (Object item : listValue) {
                sanitizedList.add(sanitizeValue(item));
            }
            return sanitizedList;
        }
        return value;
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String normalizedField = fieldName.toLowerCase(Locale.ROOT);
        for (String sensitiveKey : SENSITIVE_FIELD_KEYS) {
            if (normalizedField.contains(sensitiveKey)) {
                return true;
            }
        }
        return false;
    }
}
