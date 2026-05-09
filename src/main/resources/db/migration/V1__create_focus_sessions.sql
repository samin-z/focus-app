CREATE TABLE focus_sessions (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_seconds BIGINT,
    status VARCHAR(32) NOT NULL
);
