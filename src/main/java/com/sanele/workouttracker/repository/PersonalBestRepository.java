package com.sanele.workouttracker.repository;

import com.sanele.workouttracker.entity.Exercise;
import com.sanele.workouttracker.entity.PersonalBest;
import com.sanele.workouttracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalBestRepository extends JpaRepository<PersonalBest, Long> {

    List<PersonalBest> findByUser(User user);

    Optional<PersonalBest> findByUserAndExercise(User user, Exercise exercise);
}
