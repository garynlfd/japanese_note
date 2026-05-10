/**
 * the role of Facade
 */
package com.japanesenote.service;

import com.japanesenote.model.Note;
import com.japanesenote.model.PagedResponse;
import com.japanesenote.model.User;
import com.japanesenote.repository.NoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    public NoteService(NoteRepository noteRepository, UserService userService) {
        this.noteRepository = noteRepository;
        this.userService = userService;
    }

    public PagedResponse getNotes(String type, String username, String searchStr, Integer curPage, Integer pageSize) {
        Long userId = getUserIdByUsername(username);
        if (userId == -1) return new PagedResponse(List.of(), 0, 1, 0);

        if (pageSize > 100) pageSize = 100;

        int offset = (curPage - 1) * pageSize;
        List<Note> notes = noteRepository.findAll(userId, type, searchStr, pageSize, offset);
        log.info("get notes from all types");

        long total = count(userId, type, searchStr);
        int numOfPages = (int) Math.ceil((double) total / pageSize);

        PagedResponse pagedRes = new PagedResponse(notes, total, curPage, numOfPages);
        return pagedRes;
    }

    public Optional<Note> findById(Long id, String username) {
        Long userId = getUserIdByUsername(username);
        if(userId == -1) return Optional.empty();

        log.debug("find note with id: {}", id);
        return noteRepository.findById(id, userId);
    }

    public Note save(Note note, String username) {
        Long userId = getUserIdByUsername(username);
        if(userId == -1) return new Note();

        log.debug("save note");
        return noteRepository.save(note, userId);
    }

    public Optional<Note> update(Long id, Note note, String username) {
        Long userId = getUserIdByUsername(username);
        if(userId == -1) return Optional.empty();

        log.info("update note with id: {}", id);
        return noteRepository.update(id, note, userId);
    }

    public boolean delete(Long id, String username) {
        Long userId = getUserIdByUsername(username);
        if(userId == -1) return false;

        log.info("delete note with id: {}", id);
        return noteRepository.delete(id, userId);
    }

    public long count(Long userId, String type, String searchStr) {
        return noteRepository.count(userId, type, searchStr);
    }

    private Long getUserIdByUsername(String username) {
        Optional<User> user = this.userService.findByUsername(username);
        if (!user.isPresent()) return -1L;

        return user.get().getId();
    }
} 