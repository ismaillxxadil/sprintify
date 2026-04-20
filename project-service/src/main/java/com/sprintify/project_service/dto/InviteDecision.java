package com.sprintify.project_service.dto;

public enum InviteDecision {
    ACCEPT,
    REJECT;

    public boolean isAccept() {
        return this == ACCEPT;
    }
}
