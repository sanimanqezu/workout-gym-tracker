package com.sanele.workouttracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.workouttracker.dto.ExerciseDto;
import com.sanele.workouttracker.security.JwtAuthFilter;
import com.sanele.workouttracker.service.ExerciseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExerciseController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ExerciseController Slice Tests")
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExerciseService exerciseService;

    @Test
    @DisplayName("GET /api/exercises - returns 200 with the catalogue")
    void getAll_returnsList() throws Exception {
        when(exerciseService.getAllExercises())
                .thenReturn(List.of(new ExerciseDto(1L, "Bench Press", "Chest", "Compound push")));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value("Chest"));
    }

    @Test
    @DisplayName("POST /api/exercises - returns 201 when the request is valid")
    void create_returnsCreated() throws Exception {
        ExerciseDto request = new ExerciseDto(null, "Squat", "Legs", null);
        when(exerciseService.createExercise(any(ExerciseDto.class)))
                .thenReturn(new ExerciseDto(2L, "Squat", "Legs", null));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Squat"));
    }

    @Test
    @DisplayName("POST /api/exercises - returns 400 when required fields are missing")
    void create_validationFails() throws Exception {
        ExerciseDto request = new ExerciseDto(null, "", "", null);

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));

        verify(exerciseService, never()).createExercise(any());
    }

    @Test
    @DisplayName("POST /api/exercises - maps IllegalArgumentException to 400")
    void create_duplicate() throws Exception {
        ExerciseDto request = new ExerciseDto(null, "Bench Press", "Chest", null);
        when(exerciseService.createExercise(any(ExerciseDto.class)))
                .thenThrow(new IllegalArgumentException("Exercise 'Bench Press' already exists"));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
