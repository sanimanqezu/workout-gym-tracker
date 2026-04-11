package com.sanele.workouttracker.repository;

import com.sanele.workouttracker.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByNameIgnoreCase(String name);
}
