package com.japanesenote.service;

import org.junit.jupiter.api.Test;                                                                   
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.japanesenote.model.Note;
import com.japanesenote.model.User;
import com.japanesenote.model.PagedResponse;
import com.japanesenote.repository.NoteRepository;
import com.japanesenote.service.UserService;

@ExtendWith(MockitoExtension.class) // enables @Mock and @InjectMokcs
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository; // fake repository

    @Mock
    private UserService userService; // fake userService

    @InjectMocks
    private NoteService noteService; // real service, with fake injected

    @Test
    void getNotes_returnPagedResponse() {
        // 1. ARRANGE - set up fake data and mock behavior
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        // prepare the functions(and the return values) that are called in ACT(noteService.getNotes)
        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        when(noteRepository.findAll(1L, null, null, 20, 0)).thenReturn(List.of(mockNote));
        when(noteRepository.count(1L, null, null)).thenReturn(1L);

        // 2. ACT - call the real method
        PagedResponse res = noteService.getNotes(null, "gary", null, 1, 20);

        // 3. ASSERT - check the result
        assertEquals(1, res.getData().size());
        assertEquals(1L, res.getTotal());
        assertEquals(1, res.getCurPage());
        assertEquals(1, res.getNumOfPages());
    }

    @Test
    void getNotes_capsPageSizeAt100() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        // prepare the functions(and the return values) that are called in ACT(noteService.getNotes)
        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        // the pageSize can not be more than 100, set to 100 if exceed.
        when(noteRepository.findAll(1L, null, null, 100, 0)).thenReturn(List.of(mockNote));
        when(noteRepository.count(1L, null, null)).thenReturn(1L);

        // 2. ACT
        PagedResponse res = noteService.getNotes(null, "gary", null, 1, 200);

        // 3. ASSERT
        assertEquals(1, res.getData().size());
        assertEquals(1L, res.getTotal());
        assertEquals(1, res.getCurPage());
        assertEquals(1, res.getNumOfPages());
        // verify checks the internal call()
        // what arguments the service passed to the repo
        verify(noteRepository).findAll(1L, null, null, 100, 0);
    }

    @Test
    void getNotes_userNotFound_returnsEmpty() {
        // 1. ARRANGE
        // don't need data here because early return in findByUsername()

        // prepare the functions(and the return values) that are called in ACT(noteService.getNotes)
        when(userService.findByUsername("nobody")).thenReturn(Optional.empty());

        // 2. ACT
        PagedResponse res = noteService.getNotes(null, "nobody", null, 1, 20);

        // 3. ASSERT
        assertEquals(0, res.getData().size());
        assertEquals(0L, res.getTotal());
        assertEquals(1, res.getCurPage());
        assertEquals(0, res.getNumOfPages());
    }

    @Test
    void findById_found() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        // prepare the functions(and the return values) that are called in ACT(noteService.getNotes)
        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        when(noteRepository.findById(1L, 1L)).thenReturn(Optional.of(mockNote));

        // 2. ACT
        Optional<Note> res = noteService.findById(1L, "gary");

        // 3. ASSERT
        assertEquals(1L, res.get().getId());
        assertEquals("食べる", res.get().getTitle());
    }

    @Test
    void findById_notFound() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        // prepare the functions(and the return values) that are called in ACT(noteService.getNotes)
        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        when(noteRepository.findById(2L, 1L)).thenReturn(Optional.empty());

        // 2. ACT
        Optional<Note> res = noteService.findById(2L, "gary");

        // 3. ASSERT
        assertTrue(res.isEmpty());

        // Another correct assert: 
        // assertEquals(Optional.empty(), res);

        // Wrong assert
        // assertEquals(Optional.empty(), res.get());
        // -> res.get() unwraps the Optional, but res is empty
        // -> so .get() throws an exception
    }

    @Test
    void save_success() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        when(noteRepository.save(mockNote, 1L)).thenReturn(mockNote);

        // 2. ACT
        Note res = noteService.save(mockNote, "gary");

        // 3. ASSERT
        assertEquals(1L, res.getId());
        assertEquals("食べる", res.getTitle());
    }

    @Test
    void delete_success() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        // * We don't need a mockNote here *
        // * int test, coder decides the answer of found or not found *

        // Note mockNote = new Note();
        // mockNote.setId(1L);
        // mockNote.setTitle("食べる");

        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        // * decide true if note id is 1L *
        when(noteRepository.delete(1L, 1L)).thenReturn(true);

        // 2. ACT
        boolean res = noteService.delete(1L, "gary");

        // 3. ASSERT
        assertEquals(true, res);
    }

    @Test
    void delete_notFound() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        // * We don't need a mockNote here *
        // * int test, coder decides the answer of found or not found *

        // Note mockNote = new Note();
        // mockNote.setId(1L);
        // mockNote.setTitle("食べる");

        when(userService.findByUsername("gary")).thenReturn(Optional.of(mockUser));
        // * decide true if note id is 1L *
        when(noteRepository.delete(2L, 1L)).thenReturn(false);

        // 2. ACT
        boolean res = noteService.delete(2L, "gary");

        // 3. ASSERT
        assertEquals(false, res);
    }
}