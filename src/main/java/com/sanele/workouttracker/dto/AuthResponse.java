package com.sanele.workouttracker.dto;

public record AuthResponse(
        String token,
        String username
) {}
