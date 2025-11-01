package com.thenotesapp.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thenotesapp.api.dto.CreateNoteDto;
import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.repository.NoteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoteControllerIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static String noteId;

    @BeforeEach
    void clean() {
        noteRepository.deleteAll();
    }

    @Test
    @Order(1)
    void createNote_ShouldReturnCreatedNote() throws Exception {
        CreateNoteDto createDto = new CreateNoteDto(
                "Integration Note",
                "This is a test note for integration",
                List.of(NoteTag.IMPORTANT)
        );

        var response = mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Note"))
                .andReturn();

        var json = response.getResponse().getContentAsString();
        noteId = objectMapper.readTree(json).get("id").asText();

        assertThat(noteRepository.findAll()).hasSize(1);
    }

    @Test
    @Order(2)
    void getNoteById_ShouldReturnSummary() throws Exception {
        Note note = noteRepository.save(new Note(null, "Title", "Text", LocalDateTime.now(), List.of(NoteTag.IMPORTANT)));

        mockMvc.perform(get("/api/notes/" + note.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    @Order(3)
    void updateNote_ShouldModifyExisting() throws Exception {
        Note note = noteRepository.save(new Note(null, "Old title", "Old text",LocalDateTime.now(), List.of()));

        CreateNoteDto updateDto = new CreateNoteDto("Updated title", "Updated text", List.of(NoteTag.BUSINESS));

        mockMvc.perform(put("/api/notes/" + note.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));

        Note updated = noteRepository.findById(note.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated title");
    }

    @Test
    @Order(4)
    void getNoteText_ShouldReturnTextDto() throws Exception {
        Note note = noteRepository.save(new Note(null, "T1", "Some note text",LocalDateTime.now(), List.of()));

        mockMvc.perform(get("/api/notes/" + note.getId() + "/text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Some note text"));
    }

    @Test
    @Order(5)
    void listNotes_ShouldReturnPagedResults() throws Exception {
        noteRepository.save(new Note(null, "Note 1", "A",LocalDateTime.now(), List.of(NoteTag.PERSONAL)));
        noteRepository.save(new Note(null, "Note 2", "B",LocalDateTime.now(), List.of(NoteTag.IMPORTANT)));

        mockMvc.perform(get("/api/notes?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(6)
    void getNoteStatistics_ShouldReturnWordCounts() throws Exception {
        Note note = noteRepository.save(new Note(null, "Stats", "hello world hello", LocalDateTime.now(), List.of()));

        mockMvc.perform(get("/api/notes/" + note.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hello").value(2))
                .andExpect(jsonPath("$.world").value(1));
    }

    @Test
    @Order(7)
    void deleteNote_ShouldRemoveFromDatabase() throws Exception {
        Note note = noteRepository.save(new Note(null, "To delete", "bye",LocalDateTime.now(), List.of()));

        mockMvc.perform(delete("/api/notes/" + note.getId()))
                .andExpect(status().isNoContent());

        assertThat(noteRepository.existsById(note.getId())).isFalse();
    }
}