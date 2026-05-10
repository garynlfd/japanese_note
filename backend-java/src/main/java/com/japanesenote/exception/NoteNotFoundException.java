package com.japanesenote.exception;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super("Note not found: " + id);
    }
}