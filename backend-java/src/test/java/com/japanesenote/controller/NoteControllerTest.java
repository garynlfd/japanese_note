package com.japanesenote.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;
import java.util.Optional;

import com.japanesenote.controller.NoteController;
import com.japanesenote.model.Note;
import com.japanesenote.model.PagedResponse;
import com.japanesenote.service.NoteService;
import com.japanesenote.util.JwtUtil;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService; // fake service injected into controller

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "gary") // bypass JWT - pretend "gary" is logged in
    void getNotes_returns200() throws Exception {
        // 1. ARRANGE
        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        PagedResponse res = new PagedResponse(List.of(mockNote), 1L, 1, 1);
        when(noteService.getNotes(null, "gary", null, 1, 20)).thenReturn(res);

        // 2. ACT + 3. ASSERT (combined in MockMvc)
        mockMvc.perform(get("/api/notes")
                .param("curPage", "1")
                .param("pageSize", "20")) // closing paren for perform() is here
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.curPage").value(1))
                .andExpect(jsonPath("$.data[0].title").value("食べる"));
    }

    @Test
    @WithMockUser(username = "gary")
    void getNoteById_found_returns200() throws Exception {
        // 1. ARRANGE
        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        when(noteService.findById(1L, "gary")).thenReturn(Optional.of(mockNote));

        // 2. ACT + 3. ASSERT
        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("食べる"));
    }

    @Test
    @WithMockUser(username = "gary")
    void getNoteById_notFound_returns404() throws Exception {
        // 1. ARRANGE
        when(noteService.findById(999L, "gary")).thenReturn(Optional.empty());

        // 2. ACT + 3. ASSERT
        mockMvc.perform(get("/api/notes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "gary")
    void createNote_valid_returns201() throws Exception {
        // 1. ARRANGE
        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("食べる");

        when(noteService.save(any(Note.class), eq("gary"))).thenReturn(mockNote);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/notes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"vocab\",\"title\":\"食べる\",\"meaning\":\"to eat\",\"example\":\"私は食べる\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "gary")
    void createNote_invalid_returns400() throws Exception {
        // 1. ARRANGE

        // Spring throws MethodArgumentNotValidException when it finds that title is blank
        // -> which is @NotBlank in Note.java

        // Since this happens before calling noteService.save
        // Don't need to wrtie when(...).thenReturn(...) here

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/notes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"vocab\",\"title\":\"\",\"meaning\":\"to eat\",\"example\":\"私は食べる\"}")) // with blank title
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "gary")
    void deleteNote_found_returns204() throws Exception {
        // 1. ARRANGE
        when(noteService.delete(1L, "gary")).thenReturn(true);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(delete("/api/notes/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "gary")
    void deleteNote_notFound_returns404() throws Exception {
        // 1. ARRANGE
        when(noteService.delete(999L, "gary")).thenReturn(false);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(delete("/api/notes/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}