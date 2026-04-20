package com.sprintify.identityservice.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(MethodLoggingAspect.class);

    @Around("execution(* com.sprintify.identityservice.controller..*(..)) || " +
            "execution(* com.sprintify.identityservice.service..*(..)) || " +
            "execution(* com.sprintify.identityservice.repository..*(..))")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());

        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            logger.info("Method: {} | Args: {} | Duration: {} ms", methodName, args, durationMs);
        }
    }
}
