package com.sanele.workouttracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record WorkoutRequest(
        @NotBlank(message = "Workout name is required")
        @Size(max = 100)
        String name,

        String notes,

        @NotNull(message = "Workout date is required")
        LocalDate workoutDate,

        @Valid
        List<WorkoutSetDto> sets
) {}
