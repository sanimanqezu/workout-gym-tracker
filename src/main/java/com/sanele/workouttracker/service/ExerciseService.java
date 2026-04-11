package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.ExerciseDto;
import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    @Transactional(readOnly = true)
    public List<ExerciseDto> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ExerciseDto createExercise(ExerciseDto dto) {
        if (exerciseRepository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Exercise '" + dto.name() + "' already exists");
        }

        Exercise exercise = Exercise.builder()
                .name(dto.name())
                .muscleGroup(dto.muscleGroup())
                .description(dto.description())
                .build();

        return toDto(exerciseRepository.save(exercise));
    }

    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", id));
    }

    public ExerciseDto toDto(Exercise exercise) {
        return new ExerciseDto(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getDescription()
        );
    }
}
