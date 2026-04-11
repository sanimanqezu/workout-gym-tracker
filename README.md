# Workout Gym Tracker API

A production-ready REST API for tracking gym workouts, personal bests, and weekly training summaries. Built with Spring Boot 3 as a portfolio project demonstrating clean architecture, JWT security, and domain-driven design.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JJWT 0.12.3 |
| Persistence | Spring Data JPA + Hibernate |
| Database (prod) | PostgreSQL 16 |
| Database (test) | H2 (in-memory) |
| Migrations | Flyway |
| Build | Maven |
| Containerisation | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito |
| Boilerplate | Lombok |

---

## Getting Started

### Prerequisites
- Java 17
- Maven 3.9+
- Docker + Docker Compose (for containerised run)

### Run with Docker Compose (recommended)

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### Run locally

1. Start a PostgreSQL instance and create a database named `workout_tracker`.
2. Update credentials in `src/main/resources/application.yml` if needed.
3. Build and run:

```bash
mvn spring-boot:run
```

### Run tests

```bash
mvn test
```

---

## API Reference

### Authentication

All protected endpoints require the header:
```
Authorization: Bearer <token>
```

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register a new user, returns JWT |
| POST | `/api/auth/login` | Public | Login, returns JWT |

**Register request body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Auth response:**
```json
{
  "token": "eyJhbGci...",
  "username": "john_doe"
}
```

---

### Exercises

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/exercises` | Required | List all exercises |
| POST | `/api/exercises` | Required | Create a new exercise |

**Create exercise body:**
```json
{
  "name": "Romanian Deadlift",
  "muscleGroup": "Hamstrings",
  "description": "Hip-hinge movement targeting the posterior chain."
}
```

---

### Workouts

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/workouts` | Required | Log a new workout with sets |
| GET | `/api/workouts` | Required | List current user's workouts (newest first) |
| GET | `/api/workouts/{id}` | Required | Get workout details including all sets |
| DELETE | `/api/workouts/{id}` | Required | Delete a workout |
| GET | `/api/workouts/summary/weekly` | Required | Weekly stats for current week |

**Log workout request body:**
```json
{
  "name": "Monday Chest Day",
  "notes": "Felt strong today",
  "workoutDate": "2026-04-11",
  "sets": [
    { "exerciseId": 2, "setNumber": 1, "reps": 8, "weightKg": 100.0 },
    { "exerciseId": 2, "setNumber": 2, "reps": 8, "weightKg": 105.0 },
    { "exerciseId": 2, "setNumber": 3, "reps": 6, "weightKg": 110.0 }
  ]
}
```

**Weekly summary response:**
```json
{
  "weekStart": "2026-04-06",
  "weekEnd": "2026-04-12",
  "workoutCount": 3,
  "totalSets": 18,
  "totalReps": 162
}
```

---

### Personal Bests

Personal bests are updated automatically whenever a new workout set is logged with a higher weight than the current record for that exercise.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/personal-bests` | Required | All personal bests for current user |
| GET | `/api/personal-bests/{exerciseId}` | Required | PB for a specific exercise |

**Personal best response:**
```json
{
  "id": 1,
  "exerciseId": 2,
  "exerciseName": "Bench Press",
  "muscleGroup": "Chest",
  "weightKg": 110.0,
  "reps": 6,
  "achievedAt": "2026-04-11T09:35:00"
}
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/sanele/workouttracker/
│   │   ├── WorkoutTrackerApplication.java
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Spring Data JPA interfaces
│   │   ├── entity/           # JPA entities
│   │   ├── dto/              # Request/response records
│   │   ├── security/         # JWT filter, SecurityConfig
│   │   └── exception/        # GlobalExceptionHandler
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__create_tables.sql
└── test/
    ├── java/com/sanele/workouttracker/service/
    │   └── WorkoutServiceTest.java
    └── resources/
        └── application.yml
```

---

## Key Design Decisions

- **Stateless JWT auth** — no server-side sessions; every request is independently authenticated.
- **Personal best auto-upsert** — on every set save, the service checks and updates the PB record for that user/exercise pair.
- **Flyway migrations** — schema changes are versioned and reproducible; DDL auto is set to `validate` in production.
- **Problem Details (RFC 7807)** — all error responses use Spring's `ProblemDetail` for a consistent, standards-compliant error format.
- **Multi-stage Docker build** — the final image uses only the JRE layer and runs as a non-root user.

---

## License

MIT
