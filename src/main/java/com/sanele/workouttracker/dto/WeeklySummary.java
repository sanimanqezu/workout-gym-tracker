package com.sanele.workouttracker.dto;

import java.time.LocalDate;

public record WeeklySummary(
        LocalDate weekStart,
        LocalDate weekEnd,
        int workoutCount,
        int totalSets,
        int totalReps
) {}
