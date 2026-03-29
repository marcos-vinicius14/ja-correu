bCREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE athlete_embedding
(
    id             UUID      PRIMARY KEY,
    user_id        UUID        NOT NULL REFERENCES "tb_users" (id),
    type           VARCHAR(30) NOT NULL, -- ONBOARDING | WORKOUT | WEEKLY_SUMMARY
    reference_date DATE,
    content        TEXT        NOT NULL,
    embedding      vector(1536),
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX ON athlete_embedding USING HNSW (embedding vector_cosine_ops);
CREATE INDEX idx_athlete_embedding_user_id ON athlete_embedding (user_id);