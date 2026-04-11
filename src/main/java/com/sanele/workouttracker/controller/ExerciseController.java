package com.sanele.workouttracker.controller;

import com.sanele.workouttracker.dto.ExerciseDto;
import com.sanele.workouttracker.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<List<ExerciseDto>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    @PostMapping
    public ResponseEntity<ExerciseDto> createExercise(@Valid @RequestBody ExerciseDto dto) {
        ExerciseDto created = exerciseService.createExercise(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
