package com.sprintify.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AwardXpRequestDTO {

    private String userId;
    private int xpAmount;
}
