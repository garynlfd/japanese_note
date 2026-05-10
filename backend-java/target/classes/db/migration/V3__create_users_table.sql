CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    password   VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);