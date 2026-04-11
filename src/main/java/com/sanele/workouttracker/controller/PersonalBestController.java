package com.sanele.workouttracker.controller;

import com.sanele.workouttracker.dto.PersonalBestResponse;
import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.repository.UserRepository;
import com.sanele.workouttracker.service.PersonalBestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-bests")
@RequiredArgsConstructor
public class PersonalBestController {

    private final PersonalBestService personalBestService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<PersonalBestResponse>> getAllPersonalBests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails.getUsername());
        return ResponseEntity.ok(personalBestService.getAllPersonalBests(user));
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<PersonalBestResponse> getPersonalBestByExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long exerciseId) {
        User user = resolveUser(userDetails.getUsername());
        return ResponseEntity.ok(personalBestService.getPersonalBestByExercise(user, exerciseId));
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
