package main.java.com.sprintify.gamification_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AwardXpRequestDTO {

    @NotBlank(message = "userId is required")
    private String userId;

    @Min(value = 1, message = "xpAmount must be at least 1")
    private int xpAmount;
}
