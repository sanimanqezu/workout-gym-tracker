package com.sanele.workouttracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutSetDto(
        Long id,

        @NotNull(message = "Exercise ID is required")
        Long exerciseId,

        String exerciseName,

        @Min(value = 1, message = "Set number must be at least 1")
        int setNumber,

        @Min(value = 1, message = "Reps must be at least 1")
        int reps,

        @Positive(message = "Weight must be positive")
        double weightKg
) {}
