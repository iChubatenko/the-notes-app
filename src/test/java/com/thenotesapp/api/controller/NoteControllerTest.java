package com.thenotesapp.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thenotesapp.api.dto.*;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.service.NoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.given;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @Test
    @DisplayName("POST /api/notes - should create a note")
    void createNote_ShouldReturnCreatedNote() throws Exception {
        CreateNoteDto request = new CreateNoteDto(
                "Test title", "Some content", List.of(NoteTag.IMPORTANT));
        NoteDetailDto response = new NoteDetailDto(
                "1", "Test title", "Some content", List.of(NoteTag.IMPORTANT), LocalDateTime.now());

        given(noteService.create(any(CreateNoteDto.class))).willReturn(response);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.text").value("Some content"));
    }

    @Test
    @DisplayName("PUT /api/notes/{id} - should update a note")
    void updateNote_ShouldReturnUpdatedNote() throws Exception {
        CreateNoteDto request = new CreateNoteDto(
                "Updated title", "Updated content", List.of(NoteTag.PERSONAL));
        NoteDetailDto response = new NoteDetailDto(
                "1", "Updated title", "Updated content", List.of(NoteTag.PERSONAL), LocalDateTime.now());

        given(noteService.update(eq("1"), any(CreateNoteDto.class))).willReturn(response);

        mockMvc.perform(put("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.text").value("Updated content"));
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} - should delete a note")
    void deleteNote_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(noteService).deleteNote("1");

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} - should return 404 if not found")
    void deleteNote_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new NoSuchElementException("Note not found"))
                .when(noteService).deleteNote("999");

        mockMvc.perform(delete("/api/notes/999"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Note not found"));
    }

    @Test
    @DisplayName("GET /api/notes/{id} - should return note summary")
    void getNoteById_ShouldReturnNoteSummary() throws Exception {
        NoteSummaryDto dto = new NoteSummaryDto("1", "Note title", LocalDateTime.now());
        given(noteService.getById("1")).willReturn(Optional.of(dto));

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Note title"));
    }

    @Test
    @DisplayName("GET /api/notes/{id}/text - should return note text")
    void getNoteText_ShouldReturnText() throws Exception {
        NoteTextDto dto = new NoteTextDto("Sample text");
        given(noteService.getTextById("1")).willReturn(Optional.of(dto));

        mockMvc.perform(get("/api/notes/1/text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Sample text"));
    }

    @Test
    @DisplayName("GET /api/notes - should return paginated notes list")
    void listNotes_ShouldReturnNotesPage() throws Exception {
        List<NoteSummaryDto> list = List.of(
                new NoteSummaryDto("1", "Note A", LocalDateTime.now()),
                new NoteSummaryDto("2", "Note B", LocalDateTime.now())
        );
        PageImpl<NoteSummaryDto> page = new PageImpl<>(list, PageRequest.of(0, 10), 2);

        given(noteService.listNotes(Mockito.<List<NoteTag>>any(), Mockito.anyInt(), Mockito.anyInt()))
                .willReturn(page);

        mockMvc.perform(get("/api/notes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Note A"))
                .andExpect(jsonPath("$.content[1].title").value("Note B"));
    }

    @Test
    @DisplayName("GET /api/notes/{id}/stats - should return note statistics")
    void getNoteStatistics_ShouldReturnStats() throws Exception {
        NoteTextDto dto = new NoteTextDto("Hello world");
        Map<String, Long> stats = Map.of("words", 2L, "characters", 11L);

        given(noteService.getTextById("1")).willReturn(Optional.of(dto));
        given(noteService.getNoteStatistics("Hello world")).willReturn(stats);

        mockMvc.perform(get("/api/notes/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.words").value(2))
                .andExpect(jsonPath("$.characters").value(11));
    }

    @Test
    @DisplayName("GET /api/notes/{id}/stats - should return 404 if note not found")
    void getNoteStatistics_ShouldReturnNotFound() throws Exception {
        given(noteService.getTextById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/999/stats"))
                .andExpect(status().isNotFound());
    }

}