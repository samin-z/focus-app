CREATE INDEX idx_focus_sessions_status_end_time
    ON focus_sessions (status, end_time DESC);
