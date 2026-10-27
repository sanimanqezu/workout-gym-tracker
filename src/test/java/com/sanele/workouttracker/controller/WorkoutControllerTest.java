package com.sanele.workouttracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanele.workouttracker.dto.WeeklySummary;
import com.sanele.workouttracker.dto.WorkoutRequest;
import com.sanele.workouttracker.dto.WorkoutResponse;
import com.sanele.workouttracker.dto.WorkoutSetDto;
import com.sanele.workouttracker.exception.ResourceNotFoundException;
import com.sanele.workouttracker.security.JwtAuthFilter;
import com.sanele.workouttracker.service.WorkoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkoutController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "john_doe")
@DisplayName("WorkoutController Slice Tests")
class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private WorkoutService workoutService;

    @Test
    @DisplayName("POST /api/workouts - returns 201 and delegates with the authenticated username")
    void createWorkout_returnsCreated() throws Exception {
        WorkoutSetDto setDto = new WorkoutSetDto(null, 1L, null, 1, 10, 100.0);
        WorkoutRequest request = new WorkoutRequest("Chest Day", "Morning", LocalDate.now(), List.of(setDto));
        WorkoutResponse response = new WorkoutResponse(
                1L, "Chest Day", "Morning", LocalDate.now(), LocalDateTime.now(),
                List.of(new WorkoutSetDto(1L, 1L, "Bench Press", 1, 10, 100.0)));
        when(workoutService.createWorkout(eq("john_doe"), any(WorkoutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chest Day"))
                .andExpect(jsonPath("$.sets[0].weightKg").value(100.0));

        verify(workoutService).createWorkout(eq("john_doe"), any(WorkoutRequest.class));
    }

    @Test
    @DisplayName("POST /api/workouts - returns 400 when required fields are missing")
    void createWorkout_validationFails() throws Exception {
        WorkoutRequest request = new WorkoutRequest("", null, null, List.of());

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));

        verify(workoutService, never()).createWorkout(any(), any());
    }

    @Test
    @DisplayName("GET /api/workouts - returns 200 with the user's workouts")
    void getUserWorkouts_returnsList() throws Exception {
        WorkoutResponse response = new WorkoutResponse(
                1L, "Leg Day", null, LocalDate.now(), LocalDateTime.now(), List.of());
        when(workoutService.getUserWorkouts("john_doe")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Leg Day"));
    }

    @Test
    @DisplayName("GET /api/workouts/{id} - maps ResourceNotFoundException to 404")
    void getWorkoutById_notFound() throws Exception {
        when(workoutService.getWorkoutById("john_doe", 99L))
                .thenThrow(new ResourceNotFoundException("Workout", "id", 99L));

        mockMvc.perform(get("/api/workouts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("DELETE /api/workouts/{id} - returns 204")
    void deleteWorkout_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/workouts/1"))
                .andExpect(status().isNoContent());

        verify(workoutService).deleteWorkout("john_doe", 1L);
    }

    @Test
    @DisplayName("GET /api/workouts/summary/weekly - returns 200 with aggregated totals")
    void getWeeklySummary_returnsSummary() throws Exception {
        WeeklySummary summary = new WeeklySummary(
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(3), 2, 5, 48);
        when(workoutService.getWeeklySummary("john_doe")).thenReturn(summary);

        mockMvc.perform(get("/api/workouts/summary/weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutCount").value(2))
                .andExpect(jsonPath("$.totalSets").value(5))
                .andExpect(jsonPath("$.totalReps").value(48));
    }
}
