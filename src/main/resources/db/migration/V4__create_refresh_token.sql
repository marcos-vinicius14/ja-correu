CREATE TABLE tb_refresh_token (
    token_id UUID PRIMARY KEY,
    token VARCHAR(80) NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL,
    user_id UUID NOT NULL,

    FOREIGN KEY (user_id) REFERENCES tb_users(id)


);