-- V1__create_tables.sql
-- Workout Gym Tracker — initial schema

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE exercises (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(50)  NOT NULL,
    description  TEXT
);

CREATE TABLE workouts (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    notes        TEXT,
    workout_date DATE         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workouts_user_id ON workouts(user_id);
CREATE INDEX idx_workouts_workout_date ON workouts(workout_date);

CREATE TABLE workout_sets (
    id          BIGSERIAL PRIMARY KEY,
    workout_id  BIGINT  NOT NULL REFERENCES workouts(id)  ON DELETE CASCADE,
    exercise_id BIGINT  NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    set_number  INTEGER NOT NULL,
    reps        INTEGER NOT NULL,
    weight_kg   DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_workout_sets_workout_id ON workout_sets(workout_id);

CREATE TABLE personal_bests (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT           NOT NULL REFERENCES users(id)     ON DELETE CASCADE,
    exercise_id BIGINT           NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    weight_kg   DOUBLE PRECISION NOT NULL,
    reps        INTEGER          NOT NULL,
    achieved_at TIMESTAMP        NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, exercise_id)
);

CREATE INDEX idx_personal_bests_user_id ON personal_bests(user_id);

-- Seed some common exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
    ('Barbell Back Squat',  'Legs',       'Compound lower-body movement targeting quads, glutes, and hamstrings.'),
    ('Bench Press',         'Chest',      'Compound upper-body push targeting pectorals, triceps, and front deltoids.'),
    ('Deadlift',            'Back',       'Full-body compound pull targeting posterior chain.'),
    ('Overhead Press',      'Shoulders',  'Vertical push targeting deltoids and triceps.'),
    ('Pull-Up',             'Back',       'Bodyweight vertical pull targeting lats and biceps.'),
    ('Barbell Row',         'Back',       'Horizontal pull targeting lats, rhomboids, and biceps.'),
    ('Romanian Deadlift',   'Hamstrings', 'Hip-hinge movement targeting hamstrings and glutes.'),
    ('Dumbbell Curl',       'Biceps',     'Isolation exercise targeting the biceps brachii.'),
    ('Tricep Dip',          'Triceps',    'Compound push targeting triceps and chest.'),
    ('Leg Press',           'Legs',       'Machine compound movement targeting quads and glutes.');
