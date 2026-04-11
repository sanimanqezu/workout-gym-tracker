package com.sanele.workouttracker.repository;

import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            SELECT w FROM Workout w
            WHERE w.user = :user
              AND w.workoutDate >= :startDate
              AND w.workoutDate <= :endDate
            """)
    List<Workout> findByUserAndWorkoutDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
