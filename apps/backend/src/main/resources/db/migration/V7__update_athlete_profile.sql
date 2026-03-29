ALTER TABLE tb_athlete_profiles
    DROP COLUMN age,
    DROP COLUMN gender,
    DROP COLUMN weekly_goal_km,
    ADD COLUMN available_days_per_week SMALLINT NOT NULL,
    ADD COLUMN injuries_notes          TEXT;

ALTER TABLE tb_athlete_profiles
    ALTER COLUMN current_pace_per_km TYPE INTEGER
        USING EXTRACT(EPOCH FROM current_pace_per_km)::INTEGER;

DROP TYPE IF EXISTS athlete_gender;
