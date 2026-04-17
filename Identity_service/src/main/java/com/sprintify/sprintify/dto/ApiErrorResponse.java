package com.sprintify.sprintify.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}