package main.java.com.sprintify.gamification_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "gamification_profiles")
@Data
@NoArgsConstructor
public class GamificationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    private int currentLevel = 1;
    private int totalXp = 0;
    private int currentStreak = 0;
    private LocalDate lastActionDate;

    public GamificationProfile(String userId) {
        this.userId = userId;
    }
}
