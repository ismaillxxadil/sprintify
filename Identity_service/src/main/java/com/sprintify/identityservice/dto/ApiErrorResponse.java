package com.sprintify.identityservice.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}