ALTER TABLE athlete_embedding
ADD CONSTRAINT chk_embedding_type
CHECK (type IN ('ONBOARDING', 'WORKOUT', 'WEEKLY_SUMMARY'));
