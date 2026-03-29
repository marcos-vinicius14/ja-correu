CREATE TYPE athlete_gender AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE experience_level AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE');
CREATE TYPE athlete_goal AS ENUM ('FIVE_K', 'TEN_K', 'HALF_MARATHON', 'MARATHON');

CREATE TABLE tb_athlete_profiles
(
    id                   UUID         PRIMARY KEY,
    user_id              UUID         NOT NULL,
    age                  INT          NOT NULL,
    gender               athlete_gender NOT NULL,
    experience_level     experience_level NOT NULL,
    goal                 athlete_goal     NOT NULL,
    weekly_goal_km       NUMERIC(6, 2),
    current_pace_per_km  INTERVAL,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_athlete_profile_user
        FOREIGN KEY (user_id) REFERENCES tb_users (id) ON DELETE CASCADE,

    CONSTRAINT uq_athlete_profile_user
        UNIQUE (user_id)
);