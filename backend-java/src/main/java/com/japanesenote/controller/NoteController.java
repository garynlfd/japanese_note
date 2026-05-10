/**
 * the role of API
 */
package com.japanesenote.controller;

import com.japanesenote.exception.NoteNotFoundException;
import com.japanesenote.model.Note;
import com.japanesenote.model.PagedResponse;
import com.japanesenote.service.NoteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "http://localhost:3000")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // ── M2: GET /api/notes?type=vocab|grammar|other ────────────────────────────
    // TODO: If `type` is provided, return only notes matching that type.
    //       If omitted, return all notes.
    //       Hint: noteRepository.findByType(type) vs noteRepository.findAll()
    @GetMapping
    public ResponseEntity<PagedResponse> getNotes(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer curPage,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String searchStr) {
        // TODO: implement
        String username = getUsername();
        PagedResponse res = noteService.getNotes(type, username, searchStr, curPage, pageSize);
        return ResponseEntity.ok(res);
    }

    // ── M2: GET /api/notes/:id ─────────────────────────────────────────────────
    // TODO: Return 200 with the note, or 404 if not found.
    //       Hint: noteRepository.findById(id) returns an Optional<Note>
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        // TODO: implement
        String username = getUsername();
        Optional<Note> note = noteService.findById(id, username);

        // isPresent() return true if Optional contains a value
        //                    false if it's empty
        // use note.get() to unwrap Optional
        if (note.isPresent()) return ResponseEntity.ok(note.get());
        throw new NoteNotFoundException(id);

        // return note.isPresent() ? ResponseEntity.ok(note.get()) : ResponseEntity.notFound().build();
    }

    // ── M2: POST /api/notes ────────────────────────────────────────────────────
    // TODO: Save the note and return 201 Created with the saved note body.
    //       Hint: noteRepository.save(note), ResponseEntity.status(201).body(...)
    //       M4 upgrade: add @Valid to the parameter and add validation annotations
    //       to the Note fields (or a separate request DTO).
    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        // TODO: implement
        String username = getUsername();
        Note saved = noteService.save(note, username);
        return ResponseEntity.status(201).body(saved);
    }

    // ── M2: PUT /api/notes/:id ─────────────────────────────────────────────────
    // TODO: Replace the note entirely. Return 200 or 404.
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @Valid @RequestBody Note note) {
        // TODO: implement
        String username = getUsername();
        Optional<Note> res = noteService.update(id, note, username);

        if (res.isPresent()) return ResponseEntity.ok(res.get());
        throw new NoteNotFoundException(id);

        // return res.isPresent() ? ResponseEntity.ok(res.get()) : ResponseEntity.notFound().build();
    }

    // ── M2: DELETE /api/notes/:id ──────────────────────────────────────────────
    // TODO: Delete the note. Return 204 No Content, or 404 if not found.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        // TODO: implement
        String username = getUsername();
        boolean res = noteService.delete(id, username);

        if (res) return ResponseEntity.status(204).build();
        throw new NoteNotFoundException(id);

        // return res ? ResponseEntity.status(204).build() : ResponseEntity.notFound().build();
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
