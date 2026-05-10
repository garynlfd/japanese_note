-- index on a single column
CREATE INDEX idx_notes_type ON notes(type);

-- index on another column
CREATE INDEX idx_notes_created_at ON notes(created_at);