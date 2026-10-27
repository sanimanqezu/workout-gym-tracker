package com.sanele.workouttracker.controller;

import com.sanele.workouttracker.dto.PersonalBestResponse;
import com.sanele.workouttracker.entity.User;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.repository.UserRepository;
import com.sanele.workouttracker.security.JwtAuthFilter;
import com.sanele.workouttracker.service.PersonalBestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PersonalBestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "john_doe")
@DisplayName("PersonalBestController Slice Tests")
class PersonalBestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalBestService personalBestService;

    @MockitoBean
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("john_doe").email("john@example.com")
                .passwordHash("hash").createdAt(LocalDateTime.now()).build();
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("GET /api/personal-bests - returns 200 with the user's records")
    void getAll_returnsList() throws Exception {
        PersonalBestResponse pb = new PersonalBestResponse(
                1L, 1L, "Bench Press", "Chest", 120.0, 5, LocalDateTime.now());
        when(personalBestService.getAllPersonalBests(user)).thenReturn(List.of(pb));

        mockMvc.perform(get("/api/personal-bests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].weightKg").value(120.0));
    }

    @Test
    @DisplayName("GET /api/personal-bests/{exerciseId} - returns 200 for a tracked exercise")
    void getByExercise_returnsRecord() throws Exception {
        PersonalBestResponse pb = new PersonalBestResponse(
                1L, 1L, "Bench Press", "Chest", 120.0, 5, LocalDateTime.now());
        when(personalBestService.getPersonalBestByExercise(user, 1L)).thenReturn(pb);

        mockMvc.perform(get("/api/personal-bests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reps").value(5));
    }

    @Test
    @DisplayName("GET /api/personal-bests/{exerciseId} - maps ResourceNotFoundException to 404")
    void getByExercise_notFound() throws Exception {
        when(personalBestService.getPersonalBestByExercise(user, 99L))
                .thenThrow(new ResourceNotFoundException("Exercise", "id", 99L));

        mockMvc.perform(get("/api/personal-bests/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}
