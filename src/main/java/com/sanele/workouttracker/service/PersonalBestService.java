package com.sanele.workouttracker.service;

import com.sanele.workouttracker.dto.PersonalBestResponse;
import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.entity.PersonalBest;
import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.entity.WorkoutSet;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.ExerciseRepository;
import com.sanele.workouttracker.repository.PersonalBestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalBestService {

    private final PersonalBestRepository personalBestRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public void updatePersonalBestIfNeeded(User user, WorkoutSet set) {
        Exercise exercise = set.getExercise();
        double newWeight = set.getWeightKg();
        int newReps = set.getReps();

        Optional<PersonalBest> existing = personalBestRepository.findByUserAndExercise(user, exercise);

        if (existing.isPresent()) {
            PersonalBest pb = existing.get();
            if (newWeight > pb.getWeightKg()) {
                pb.setWeightKg(newWeight);
                pb.setReps(newReps);
                pb.setAchievedAt(LocalDateTime.now());
                personalBestRepository.save(pb);
            }
        } else {
            PersonalBest newPb = PersonalBest.builder()
                    .user(user)
                    .exercise(exercise)
                    .weightKg(newWeight)
                    .reps(newReps)
                    .achievedAt(LocalDateTime.now())
                    .build();
            personalBestRepository.save(newPb);
        }
    }

    @Transactional(readOnly = true)
    public List<PersonalBestResponse> getAllPersonalBests(User user) {
        return personalBestRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonalBestResponse getPersonalBestByExercise(User user, Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));

        PersonalBest pb = personalBestRepository.findByUserAndExercise(user, exercise)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No personal best found for exercise: " + exercise.getName()));

        return toResponse(pb);
    }

    private PersonalBestResponse toResponse(PersonalBest pb) {
        return new PersonalBestResponse(
                pb.getId(),
                pb.getExercise().getId(),
                pb.getExercise().getName(),
                pb.getExercise().getMuscleGroup(),
                pb.getWeightKg(),
                pb.getReps(),
                pb.getAchievedAt()
        );
    }
}
