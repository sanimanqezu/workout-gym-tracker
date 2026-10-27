package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.PersonalBestResponse;
import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.entity.PersonalBest;
import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.entity.WorkoutSet;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.ExerciseRepository;
import com.sanele.workouttracker.repository.PersonalBestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalBestService Tests")
class PersonalBestServiceTest {

    @Mock
    private PersonalBestRepository personalBestRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private PersonalBestService personalBestService;

    private User user;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("john_doe").email("john@example.com")
                .passwordHash("hash").createdAt(LocalDateTime.now()).build();
        exercise = Exercise.builder().id(1L).name("Bench Press").muscleGroup("Chest").build();
    }

    @Test
    @DisplayName("updatePersonalBestIfNeeded: creates a new record when none exists")
    void updatePersonalBest_createsNew() {
        WorkoutSet set = WorkoutSet.builder().exercise(exercise).setNumber(1).reps(5).weightKg(120.0).build();
        when(personalBestRepository.findByUserAndExercise(user, exercise)).thenReturn(Optional.empty());

        personalBestService.updatePersonalBestIfNeeded(user, set);

        ArgumentCaptor<PersonalBest> captor = ArgumentCaptor.forClass(PersonalBest.class);
        verify(personalBestRepository).save(captor.capture());
        assertThat(captor.getValue().getWeightKg()).isEqualTo(120.0);
        assertThat(captor.getValue().getReps()).isEqualTo(5);
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("updatePersonalBestIfNeeded: updates the record when the new weight is heavier")
    void updatePersonalBest_updatesWhenHeavier() {
        PersonalBest existing = PersonalBest.builder()
                .id(10L).user(user).exercise(exercise).weightKg(100.0).reps(8)
                .achievedAt(LocalDateTime.now().minusDays(5)).build();
        WorkoutSet set = WorkoutSet.builder().exercise(exercise).setNumber(1).reps(3).weightKg(110.0).build();
        when(personalBestRepository.findByUserAndExercise(user, exercise)).thenReturn(Optional.of(existing));

        personalBestService.updatePersonalBestIfNeeded(user, set);

        assertThat(existing.getWeightKg()).isEqualTo(110.0);
        assertThat(existing.getReps()).isEqualTo(3);
        verify(personalBestRepository).save(existing);
    }

    @Test
    @DisplayName("updatePersonalBestIfNeeded: does nothing when the new weight is not heavier")
    void updatePersonalBest_noUpdateWhenLighter() {
        PersonalBest existing = PersonalBest.builder()
                .id(10L).user(user).exercise(exercise).weightKg(100.0).reps(8)
                .achievedAt(LocalDateTime.now().minusDays(5)).build();
        WorkoutSet set = WorkoutSet.builder().exercise(exercise).setNumber(1).reps(12).weightKg(90.0).build();
        when(personalBestRepository.findByUserAndExercise(user, exercise)).thenReturn(Optional.of(existing));

        personalBestService.updatePersonalBestIfNeeded(user, set);

        assertThat(existing.getWeightKg()).isEqualTo(100.0);
        verify(personalBestRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllPersonalBests: maps every record to a response")
    void getAllPersonalBests_mapsAll() {
        PersonalBest pb = PersonalBest.builder()
                .id(1L).user(user).exercise(exercise).weightKg(100.0).reps(8)
                .achievedAt(LocalDateTime.now()).build();
        when(personalBestRepository.findByUser(user)).thenReturn(List.of(pb));

        List<PersonalBestResponse> result = personalBestService.getAllPersonalBests(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).exerciseName()).isEqualTo("Bench Press");
        assertThat(result.get(0).weightKg()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("getPersonalBestByExercise: returns the record when it exists")
    void getPersonalBestByExercise_success() {
        PersonalBest pb = PersonalBest.builder()
                .id(1L).user(user).exercise(exercise).weightKg(100.0).reps(8)
                .achievedAt(LocalDateTime.now()).build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(personalBestRepository.findByUserAndExercise(user, exercise)).thenReturn(Optional.of(pb));

        PersonalBestResponse result = personalBestService.getPersonalBestByExercise(user, 1L);

        assertThat(result.exerciseId()).isEqualTo(1L);
        assertThat(result.reps()).isEqualTo(8);
    }

    @Test
    @DisplayName("getPersonalBestByExercise: throws when the exercise does not exist")
    void getPersonalBestByExercise_exerciseNotFound() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalBestService.getPersonalBestByExercise(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise");
    }

    @Test
    @DisplayName("getPersonalBestByExercise: throws when no personal best is recorded")
    void getPersonalBestByExercise_noPersonalBest() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(personalBestRepository.findByUserAndExercise(user, exercise)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalBestService.getPersonalBestByExercise(user, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No personal best");
    }
}
