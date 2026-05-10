/**
 * the role of DAO
 */
package com.japanesenote.repository;

import com.japanesenote.model.Note;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class NoteRepository {

    private final JdbcTemplate jdbc;

    public NoteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Maps a database row → Note object. You'll reuse this in every query.
    private final RowMapper<Note> rowMapper = (rs, rowNum) -> {
        Note note = new Note();
        // TODO: read each column from `rs` and set it on `note`

        // id, type, title, meaning, example, createdAt
        note.setId(rs.getLong("id"));
        note.setType(rs.getString("type"));
        note.setTitle(rs.getString("title"));
        note.setMeaning(rs.getString("meaning"));
        note.setExample(rs.getString("example"));
        note.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        note.setUserId(rs.getLong("user_id"));

        return note;
    };

    // Get a row from Postgres, we need "rowMapper" to map the row into the data type we want(here is Note)
    // so we need rowMapper in jdbc.query().
    // jdbc.query() will also collect all Note objects in to List<Node> and return it

    // ── M2: findAll ────────────────────────────────────────────────────────────
    public List<Note> findAll(Long userId, String type, String searchStr, int limit, int offset) {
        String sql = "SELECT * FROM notes WHERE user_id = ?";
        List<Object> params = new ArrayList<>();                                                           
        params.add(userId);

        if (type != null) {
            sql += " AND type = ?";
            params.add(type);
        }

        if (searchStr != null) {
            sql += " AND (title ILIKE ? OR example ILIKE ? OR meaning ILIKE ?)";
            params.add("%" + searchStr + "%");
            params.add("%" + searchStr + "%");
            params.add("%" + searchStr + "%");
        }

        sql += " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql, rowMapper, params.toArray());
    }

    // ── M2: findByType ─────────────────────────────────────────────────────────
    /**
     * Deprecated after M8: pagination
     * Use params to add non null value into sql query
     */
    public List<Note> findByType(String type, Long userId) {
        String sql = "SELECT * FROM notes WHERE type = ? AND user_id = ?";
        return jdbc.query(sql, rowMapper, type, userId);
    }

    // ── M2: findById ───────────────────────────────────────────────────────────
    public Optional<Note> findById(Long id, Long userId) {
        String sql = "SELECT * FROM notes WHERE id = ? AND user_id = ?";
        List<Note> results = jdbc.query(sql, rowMapper, id, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // ── M2: save (INSERT) ──────────────────────────────────────────────────────
    public Note save(Note note, Long userId) {
        String sql = "INSERT INTO notes (type, title, meaning, example, user_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
        // Hint: use jdbc.queryForObject(sql, Long.class, ...) to get the generated id back
        //       then set it on the note and return it

        // jdbc.queryForObject() returns exactly 1 row
        // jdbc.query() returns List<T> 0 or more rows
        Long id = jdbc.queryForObject(sql, Long.class, note.getType(), note.getTitle(), note.getMeaning(), note.getExample(), userId);
        note.setId(id);
        return note;
    }

    // ── M2: update (UPDATE) ────────────────────────────────────────────────────
    public Optional<Note> update(Long id, Note note, Long userId) {
        String sql = "UPDATE notes SET type = ?, title = ?, meaning = ?, example = ? WHERE id = ? AND user_id = ?";
        // Hint: jdbc.update(sql, ...) returns the number of affected rows
        //       return Optional.empty() if 0 rows were affected (id not found)
        int res = jdbc.update(sql, note.getType(), note.getTitle(), note.getMeaning(), note.getExample(), id, userId);
        return res > 0 ? Optional.of(note) : Optional.empty();
    }

    // ── M2: delete ─────────────────────────────────────────────────────────────
    public boolean delete(Long id, Long userId) {
        String sql = "DELETE FROM notes WHERE id = ? AND user_id = ?";
        // Hint: jdbc.update(sql, id) returns affected row count
        //       return true if 1 row was deleted, false if 0 (not found)
        int res = jdbc.update(sql, id, userId);
        return res > 0 ? true : false;
    }

    public long count(Long userId, String type, String searchStr) {
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null) {
            sql += " AND type = ?";
            params.add(type);
        }

        if (searchStr != null) {
            sql += " AND (title ILIKE ? OR example ILIKE ?)";
            params.add("%" + searchStr + "%");
            params.add("%" + searchStr + "%");
        }
        return jdbc.queryForObject(sql, Long.class, params.toArray());
    }
}
