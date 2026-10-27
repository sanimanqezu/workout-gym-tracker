# Workout Gym Tracker API

[![CI](https://github.com/sanimanqezu/workout-gym-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/sanimanqezu/workout-gym-tracker/actions/workflows/ci.yml)

A REST API for tracking gym workouts, personal bests, and weekly training summaries, secured with stateless JWT authentication so every user sees only their own data.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5.16 |
| Security | Spring Security + JJWT 0.12.3 |
| Persistence | Spring Data JPA + Hibernate |
| Database (runtime) | PostgreSQL |
| Database (test) | H2 (in-memory) |
| Migrations | Flyway |
| Build | Maven |
| Containerisation | Docker + Docker Compose |
| Testing | JUnit 5, Mockito, AssertJ, MockMvc, Spring Security Test |
| Boilerplate | Lombok |

---

## Key Design Points

- **Stateless JWT auth** — a custom `OncePerRequestFilter` validates the bearer token and populates the security context; sessions are `STATELESS` and all routes except `/api/auth/**` require authentication.
- **Per-user data isolation** — workout and personal-best reads are scoped to the authenticated username; requesting another user's workout returns 404 rather than leaking its existence.
- **Automatic personal-best upsert** — each logged set is compared against the user's record for that exercise and the personal best is updated only when the new weight is heavier.
- **Layered architecture** — thin controllers resolve the principal and delegate to transactional services; entities never cross the API boundary (DTOs are immutable records with Jakarta Bean Validation).
- **Problem Details (RFC 7807)** — a single `@RestControllerAdvice` maps `ResourceNotFoundException` to 404, `BadCredentialsException` to 401, `IllegalArgumentException` and validation failures to 400, as `ProblemDetail` bodies.
- **Database migrations as code** — schema is owned by Flyway; Hibernate runs in `ddl-auto: validate`.

---

## Getting Started

### Prerequisites
- Java 25
- Maven 3.9+
- The shared portfolio PostgreSQL container running

### Database

This service connects to a **centralized PostgreSQL** shared across the portfolio rather than its own database. The connection is driven entirely by environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) — see `.env.example`.

```bash
# Start the centralized Postgres once, from the portfolio root
docker compose up -d
```

### Run locally

```bash
# Defaults connect to localhost:5432/workout_tracker
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Run with Docker Compose

The repo's `docker-compose.yml` builds the app image and attaches it to the external `portfolio-net` network so it can reach the shared database:

```bash
docker compose up --build
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
    ├── java/com/sanele/workouttracker/
    │   ├── controller/       # @WebMvcTest slice tests
    │   └── service/          # Mockito unit tests
    └── resources/
        └── application.yml
```

---

## Tests

```bash
mvn test
```

The suite is **42 tests** and needs no database or network — it runs against mocks and an in-memory H2 schema.

- **Service-layer unit tests** (`@ExtendWith(MockitoExtension.class)`, `@Mock` repositories, `@InjectMocks` service) cover each service's happy path, validation failures, not-found cases, and edge cases — including registration with duplicate username/email, workout creation with and without sets, per-user ownership checks on reads and deletes, the weekly-summary aggregation, and the personal-best upsert logic (new record, heavier lift, and no-op when lighter).
- **Controller slice tests** (`@WebMvcTest`, `@MockitoBean` service) verify status codes and request delegation, that invalid payloads return **400**, and that the `GlobalExceptionHandler` maps domain exceptions to the right status (`ResourceNotFoundException` → **404**, `BadCredentialsException` → **401**, `IllegalArgumentException` → **400**) as `ProblemDetail` bodies. Secured endpoints are exercised with `@WithMockUser`, with the JWT filter excluded from the web slice so the tests target the controller contract.

---

## License

MIT
