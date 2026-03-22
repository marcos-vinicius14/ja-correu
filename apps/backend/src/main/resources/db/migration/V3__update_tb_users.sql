ALTER TABLE tb_users
    ADD strava_acess_token VARCHAR(255);

ALTER TABLE tb_users
    ADD strava_expires_at BIGINT;

ALTER TABLE tb_users
    ADD strava_refresh_token VARCHAR(255);

ALTER TABLE tb_users
    ADD CONSTRAINT uc_tb_users_strava_acess_token UNIQUE (strava_acess_token);

ALTER TABLE tb_users
    ADD CONSTRAINT uc_tb_users_strava_refresh_token UNIQUE (strava_refresh_token);

ALTER TABLE tb_users
    DROP COLUMN strava_token;

ALTER TABLE tb_users
    ALTER COLUMN created_at SET NOT NULL;