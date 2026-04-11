package com.sanele.workouttracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseDto(
        Long id,

        @NotBlank(message = "Exercise name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Muscle group is required")
        @Size(max = 50)
        String muscleGroup,

        String description
) {}
