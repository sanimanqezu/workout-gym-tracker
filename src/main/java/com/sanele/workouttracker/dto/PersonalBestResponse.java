package com.sanele.workouttracker.dto;

import java.time.LocalDateTime;

public record PersonalBestResponse(
        Long id,
        Long exerciseId,
        String exerciseName,
        String muscleGroup,
        double weightKg,
        int reps,
        LocalDateTime achievedAt
) {}
