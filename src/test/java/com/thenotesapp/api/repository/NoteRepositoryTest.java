package com.thenotesapp.api.repository;

import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class NoteRepositoryTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @DynamicPropertySource
    static void setMongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setup() {
        noteRepository.deleteAll();

        noteRepository.saveAll(List.of(
                new Note(null, "Note 1", "Text 1", LocalDateTime.now(), List.of(NoteTag.IMPORTANT)),
                new Note(null, "Note 2", "Text 2", LocalDateTime.now(), List.of(NoteTag.BUSINESS, NoteTag.IMPORTANT)),
                new Note(null, "Note 3", "Text 3", LocalDateTime.now(), List.of(NoteTag.BUSINESS))
        ));
    }

    @Test
    @DisplayName("Should find notes by tag")
    void findByTagsIn_ShouldReturnMatchingNotes() {
        Page<Note> page = noteRepository.findByTagsIn(List.of(NoteTag.IMPORTANT), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent())
                .extracting(Note::getTitle)
                .containsExactlyInAnyOrder("Note 1", "Note 2");
    }

    @Test
    @DisplayName("Should return empty page when no matching tags found")
    void findByTagsIn_ShouldReturnEmpty() {
        Page<Note> page = noteRepository.findByTagsIn(List.of(NoteTag.PERSONAL), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should store and retrieve notes correctly")
    void saveAndFindAll_ShouldWorkCorrectly() {
        Note saved = noteRepository.save(new Note(null, "New Note", "Some text", LocalDateTime.now(), List.of(NoteTag.BUSINESS)));

        assertThat(saved.getId()).isNotNull();

        List<Note> all = noteRepository.findAll();
        assertThat(all).hasSize(4);
    }
}