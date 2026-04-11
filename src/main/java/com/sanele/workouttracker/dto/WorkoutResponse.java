package com.sanele.workouttracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponse(
        Long id,
        String name,
        String notes,
        LocalDate workoutDate,
        LocalDateTime createdAt,
        List<WorkoutSetDto> sets
) {}
