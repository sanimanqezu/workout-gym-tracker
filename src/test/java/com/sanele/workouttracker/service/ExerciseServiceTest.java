package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.ExerciseDto;
import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseService Tests")
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private Exercise benchPress;

    @BeforeEach
    void setUp() {
        benchPress = Exercise.builder()
                .id(1L).name("Bench Press").muscleGroup("Chest").description("Compound push movement").build();
    }

    @Test
    @DisplayName("getAllExercises: maps every entity to a DTO")
    void getAllExercises_mapsAll() {
        when(exerciseRepository.findAll()).thenReturn(List.of(benchPress));

        List<ExerciseDto> result = exerciseService.getAllExercises();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Bench Press");
        assertThat(result.get(0).muscleGroup()).isEqualTo("Chest");
    }

    @Test
    @DisplayName("createExercise: persists a new exercise when the name is unique")
    void createExercise_success() {
        ExerciseDto dto = new ExerciseDto(null, "Squat", "Legs", "Compound leg movement");
        when(exerciseRepository.existsByNameIgnoreCase("Squat")).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> {
            Exercise e = inv.getArgument(0);
            return Exercise.builder().id(2L).name(e.getName()).muscleGroup(e.getMuscleGroup())
                    .description(e.getDescription()).build();
        });

        ExerciseDto result = exerciseService.createExercise(dto);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Squat");
        ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
        verify(exerciseRepository).save(captor.capture());
        assertThat(captor.getValue().getMuscleGroup()).isEqualTo("Legs");
    }

    @Test
    @DisplayName("createExercise: rejects a duplicate name with IllegalArgumentException")
    void createExercise_duplicateName() {
        ExerciseDto dto = new ExerciseDto(null, "Bench Press", "Chest", null);
        when(exerciseRepository.existsByNameIgnoreCase("Bench Press")).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.createExercise(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById: returns the entity when present")
    void findById_success() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(benchPress));

        Exercise found = exerciseService.findById(1L);

        assertThat(found).isSameAs(benchPress);
    }

    @Test
    @DisplayName("findById: throws ResourceNotFoundException when missing")
    void findById_notFound() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise")
                .hasMessageContaining("99");
    }
}
