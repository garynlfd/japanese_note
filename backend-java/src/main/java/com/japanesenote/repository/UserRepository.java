package com.japanesenote.repository;

import com.japanesenote.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return user;
    };

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, rowMapper);
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> res = jdbc.query(sql, rowMapper, id);

        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> res = jdbc.query(sql, rowMapper, username);

        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    public User save(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING id";

        Long id = jdbc.queryForObject(sql, Long.class, user.getUsername(), user.getPassword());
        user.setId(id);
        return user;
    }

    public Optional<User> update(Long id, User user) {
        // Q: why we need to receive parameter id?
        // -> we can use user.getId to get it
        // A: Yes, but there mey have risk
        // -> e.g., The client sends the ID in the URL (PUT /api/notes/99), not in the body.
        // -> The body's id field might be null or different. 

        // Q: can use write sql like we do in INSERT?
        // -> (xxx, xxx, xxx) VALUES (?, ?, ?)
        // A: NO, it's a fixed syntax.

        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        int res = jdbc.update(sql, user.getUsername(), user.getPassword(), id);

        return res > 0 ? Optional.of(user) : Optional.empty();
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int res = jdbc.update(sql, id);

        return res > 0;
    }
}