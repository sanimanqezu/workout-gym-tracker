package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.WorkoutRequest;
import com.sanele.workouttracker.dto.WorkoutResponse;
import com.sanele.workouttracker.dto.WorkoutSetDto;
import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.entity.Workout;
import com.sanele.workouttracker.entity.WorkoutSet;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.UserRepository;
import com.sanele.workouttracker.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private PersonalBestService personalBestService;

    @InjectMocks
    private WorkoutService workoutService;

    private User testUser;
    private Exercise testExercise;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .passwordHash("$2a$10$hashed")
                .createdAt(LocalDateTime.now())
                .build();

        testExercise = Exercise.builder()
                .id(1L)
                .name("Bench Press")
                .muscleGroup("Chest")
                .description("Compound push movement")
                .build();
    }

    // --- Test 1: createWorkout — success with sets ---

    @Test
    @DisplayName("createWorkout: saves workout with sets and returns response")
    void createWorkout_withSets_returnsWorkoutResponse() {
        WorkoutSetDto setDto = new WorkoutSetDto(null, 1L, null, 1, 10, 100.0);
        WorkoutRequest request = new WorkoutRequest("Chest Day", "Morning session", LocalDate.now(), List.of(setDto));

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
        when(exerciseService.findById(1L)).thenReturn(testExercise);

        Workout savedWorkout = buildSavedWorkout(1L, testUser, request, List.of(
                buildSet(1L, testExercise, 1, 10, 100.0)
        ));

        when(workoutRepository.save(any(Workout.class))).thenReturn(savedWorkout);
        doNothing().when(personalBestService).updatePersonalBestIfNeeded(any(), any());

        WorkoutResponse response = workoutService.createWorkout("john_doe", request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Chest Day");
        assertThat(response.sets()).hasSize(1);
        assertThat(response.sets().get(0).weightKg()).isEqualTo(100.0);
        verify(workoutRepository).save(any(Workout.class));
        verify(personalBestService).updatePersonalBestIfNeeded(eq(testUser), any(WorkoutSet.class));
    }

    // --- Test 2: createWorkout — no sets ---

    @Test
    @DisplayName("createWorkout: saves workout with empty sets list")
    void createWorkout_withoutSets_savesSuccessfully() {
        WorkoutRequest request = new WorkoutRequest("Rest Day Log", null, LocalDate.now(), List.of());

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        Workout savedWorkout = buildSavedWorkout(2L, testUser, request, new ArrayList<>());
        when(workoutRepository.save(any(Workout.class))).thenReturn(savedWorkout);

        WorkoutResponse response = workoutService.createWorkout("john_doe", request);

        assertThat(response.sets()).isEmpty();
        verify(personalBestService, never()).updatePersonalBestIfNeeded(any(), any());
    }

    // --- Test 3: getUserWorkouts — returns sorted list ---

    @Test
    @DisplayName("getUserWorkouts: returns all workouts for user ordered by creation date")
    void getUserWorkouts_returnsAllWorkoutsForUser() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        Workout w1 = buildSavedWorkout(1L, testUser, new WorkoutRequest("Leg Day", null, LocalDate.now(), List.of()), new ArrayList<>());
        Workout w2 = buildSavedWorkout(2L, testUser, new WorkoutRequest("Back Day", null, LocalDate.now().minusDays(1), List.of()), new ArrayList<>());

        when(workoutRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(w1, w2));

        List<WorkoutResponse> workouts = workoutService.getUserWorkouts("john_doe");

        assertThat(workouts).hasSize(2);
        assertThat(workouts.get(0).name()).isEqualTo("Leg Day");
        assertThat(workouts.get(1).name()).isEqualTo("Back Day");
    }

    // --- Test 4: getWorkoutById — success ---

    @Test
    @DisplayName("getWorkoutById: returns correct workout when user owns it")
    void getWorkoutById_whenUserOwnsWorkout_returnsResponse() {
        Workout workout = buildSavedWorkout(5L, testUser,
                new WorkoutRequest("Shoulder Day", "Heavy", LocalDate.now(), List.of()), new ArrayList<>());

        when(workoutRepository.findById(5L)).thenReturn(Optional.of(workout));

        WorkoutResponse response = workoutService.getWorkoutById("john_doe", 5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Shoulder Day");
    }

    // --- Test 5: getWorkoutById — not found ---

    @Test
    @DisplayName("getWorkoutById: throws ResourceNotFoundException when workout does not exist")
    void getWorkoutById_whenWorkoutNotFound_throwsException() {
        when(workoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.getWorkoutById("john_doe", 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- Test 6: getWorkoutById — unauthorised access ---

    @Test
    @DisplayName("getWorkoutById: throws ResourceNotFoundException when another user owns the workout")
    void getWorkoutById_whenWorkoutBelongsToOtherUser_throwsException() {
        User otherUser = User.builder().id(2L).username("other_user").email("other@example.com")
                .passwordHash("hash").createdAt(LocalDateTime.now()).build();

        Workout workout = buildSavedWorkout(7L, otherUser,
                new WorkoutRequest("Other's Workout", null, LocalDate.now(), List.of()), new ArrayList<>());

        when(workoutRepository.findById(7L)).thenReturn(Optional.of(workout));

        assertThatThrownBy(() -> workoutService.getWorkoutById("john_doe", 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Test 7: deleteWorkout — success ---

    @Test
    @DisplayName("deleteWorkout: deletes workout when user owns it")
    void deleteWorkout_whenUserOwnsWorkout_deletesSuccessfully() {
        Workout workout = buildSavedWorkout(3L, testUser,
                new WorkoutRequest("Arms Day", null, LocalDate.now(), List.of()), new ArrayList<>());

        when(workoutRepository.findById(3L)).thenReturn(Optional.of(workout));
        doNothing().when(workoutRepository).delete(workout);

        workoutService.deleteWorkout("john_doe", 3L);

        verify(workoutRepository).delete(workout);
    }

    // --- Test 8: getWeeklySummary — correct aggregation ---

    @Test
    @DisplayName("getWeeklySummary: aggregates sets and reps for current week")
    void getWeeklySummary_returnsCorrectAggregation() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        WorkoutSet set1 = buildSet(1L, testExercise, 1, 8, 80.0);
        WorkoutSet set2 = buildSet(2L, testExercise, 2, 10, 80.0);
        WorkoutSet set3 = buildSet(3L, testExercise, 1, 12, 60.0);

        Workout w1 = buildSavedWorkout(1L, testUser,
                new WorkoutRequest("Monday Push", null, LocalDate.now(), List.of()), List.of(set1, set2));
        Workout w2 = buildSavedWorkout(2L, testUser,
                new WorkoutRequest("Wednesday Pull", null, LocalDate.now(), List.of()), List.of(set3));

        when(workoutRepository.findByUserAndWorkoutDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(w1, w2));

        var summary = workoutService.getWeeklySummary("john_doe");

        assertThat(summary.workoutCount()).isEqualTo(2);
        assertThat(summary.totalSets()).isEqualTo(3);
        assertThat(summary.totalReps()).isEqualTo(30); // 8 + 10 + 12
        assertThat(summary.weekStart()).isNotNull();
        assertThat(summary.weekEnd()).isNotNull();
    }

    // ---- Helper methods ----

    private Workout buildSavedWorkout(Long id, User user, WorkoutRequest request, List<WorkoutSet> sets) {
        Workout workout = Workout.builder()
                .id(id)
                .user(user)
                .name(request.name())
                .notes(request.notes())
                .workoutDate(request.workoutDate())
                .createdAt(LocalDateTime.now())
                .sets(new ArrayList<>(sets))
                .build();

        // Link sets back to workout
        for (WorkoutSet set : workout.getSets()) {
            set.setWorkout(workout);
        }
        return workout;
    }

    private WorkoutSet buildSet(Long id, Exercise exercise, int setNumber, int reps, double weight) {
        return WorkoutSet.builder()
                .id(id)
                .exercise(exercise)
                .setNumber(setNumber)
                .reps(reps)
                .weightKg(weight)
                .build();
    }
}
