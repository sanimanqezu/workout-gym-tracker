package com.sanele.workouttracker.controller;

import com.sanele.workouttracker.dto.WeeklySummary;
import com.sanele.workouttracker.dto.WorkoutRequest;
import com.sanele.workouttracker.dto.WorkoutResponse;
import com.sanele.workouttracker.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkoutRequest request) {
        WorkoutResponse response = workoutService.createWorkout(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getUserWorkouts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutService.getUserWorkouts(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkoutById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutById(userDetails.getUsername(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        workoutService.deleteWorkout(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/weekly")
    public ResponseEntity<WeeklySummary> getWeeklySummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutService.getWeeklySummary(userDetails.getUsername()));
    }
}
