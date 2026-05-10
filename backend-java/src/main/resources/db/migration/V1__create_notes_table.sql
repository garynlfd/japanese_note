CREATE TABLE notes (
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(20) NOT NULL,
    title      VARCHAR(20) NOT NULL,
    meaning    TEXT,
    example    TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);