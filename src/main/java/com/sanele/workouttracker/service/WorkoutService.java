package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.WeeklySummary;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseService exerciseService;
    private final PersonalBestService personalBestService;

    @Transactional
    public WorkoutResponse createWorkout(String username, WorkoutRequest request) {
        User user = findUser(username);

        Workout workout = Workout.builder()
                .user(user)
                .name(request.name())
                .notes(request.notes())
                .workoutDate(request.workoutDate())
                .sets(new ArrayList<>())
                .build();

        if (request.sets() != null) {
            for (WorkoutSetDto setDto : request.sets()) {
                Exercise exercise = exerciseService.findById(setDto.exerciseId());

                WorkoutSet set = WorkoutSet.builder()
                        .workout(workout)
                        .exercise(exercise)
                        .setNumber(setDto.setNumber())
                        .reps(setDto.reps())
                        .weightKg(setDto.weightKg())
                        .build();

                workout.getSets().add(set);
            }
        }

        Workout saved = workoutRepository.save(workout);

        // Check personal bests after persisting sets
        for (WorkoutSet set : saved.getSets()) {
            personalBestService.updatePersonalBestIfNeeded(user, set);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> getUserWorkouts(String username) {
        User user = findUser(username);
        return workoutRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(String username, Long workoutId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", workoutId));

        if (!workout.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Workout", "id", workoutId);
        }

        return toResponse(workout);
    }

    @Transactional
    public void deleteWorkout(String username, Long workoutId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", workoutId));

        if (!workout.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Workout", "id", workoutId);
        }

        workoutRepository.delete(workout);
    }

    @Transactional(readOnly = true)
    public WeeklySummary getWeeklySummary(String username) {
        User user = findUser(username);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        List<Workout> weeklyWorkouts = workoutRepository
                .findByUserAndWorkoutDateBetween(user, weekStart, weekEnd);

        int totalSets = weeklyWorkouts.stream()
                .mapToInt(w -> w.getSets().size())
                .sum();

        int totalReps = weeklyWorkouts.stream()
                .flatMap(w -> w.getSets().stream())
                .mapToInt(WorkoutSet::getReps)
                .sum();

        return new WeeklySummary(weekStart, weekEnd, weeklyWorkouts.size(), totalSets, totalReps);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private WorkoutResponse toResponse(Workout workout) {
        List<WorkoutSetDto> setDtos = workout.getSets().stream()
                .map(s -> new WorkoutSetDto(
                        s.getId(),
                        s.getExercise().getId(),
                        s.getExercise().getName(),
                        s.getSetNumber(),
                        s.getReps(),
                        s.getWeightKg()
                ))
                .toList();

        return new WorkoutResponse(
                workout.getId(),
                workout.getName(),
                workout.getNotes(),
                workout.getWorkoutDate(),
                workout.getCreatedAt(),
                setDtos
        );
    }
}
