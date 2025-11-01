package com.thenotesapp.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thenotesapp.api.dto.CreateNoteDto;
import com.thenotesapp.api.dto.NoteDetailDto;
import com.thenotesapp.api.dto.NoteSummaryDto;
import com.thenotesapp.api.dto.NoteTextDto;
import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    private ObjectMapper objectMapper;

    @InjectMocks
    private NoteService noteService;

    private Note note;
    private CreateNoteDto createDto;
    private NoteDetailDto detailDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        noteService = new NoteService(noteRepository, objectMapper);

        note = new Note();
        note.setId("1");
        note.setTitle("Test title");
        note.setText("This is a test note text");
        note.setTags(List.of(NoteTag.PERSONAL));
        note.setCreatedDate(LocalDateTime.now());

        createDto = new CreateNoteDto();
        createDto.setTitle("Test title");
        createDto.setText("This is a test note text");
        createDto.setTags(List.of(NoteTag.PERSONAL));

        detailDto = new NoteDetailDto(
                note.getId(),
                note.getTitle(),
                note.getText(),
                note.getTags(),
                note.getCreatedDate()
        );
    }

    @Test
    void create_ShouldSaveAndReturnNoteDetailDto() {

        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteDetailDto result = noteService.create(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(createDto.getTitle());
        assertThat(result.getText()).isEqualTo(createDto.getText());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void update_ShouldUpdateExistingNote() {
        when(noteRepository.findById("1")).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteDetailDto result = noteService.update("1", createDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(createDto.getTitle());
        assertThat(result.getText()).isEqualTo(createDto.getText());
        verify(noteRepository).findById("1");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void update_ShouldThrowIfNoteNotFound() {
        when(noteRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.update("999", createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void deleteNote_ShouldDeleteIfExists() {
        when(noteRepository.existsById("1")).thenReturn(true);

        noteService.deleteNote("1");

        verify(noteRepository).deleteById("1");
    }

    @Test
    void deleteNote_ShouldThrowIfNotFound() {
        when(noteRepository.existsById("999")).thenReturn(false);

        assertThatThrownBy(() -> noteService.deleteNote("999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Note not found with id: 999");
    }

    @Test
    void getById_ShouldReturnSummaryDto() {
        when(noteRepository.findById("1")).thenReturn(Optional.of(note));

        Optional<NoteSummaryDto> result = noteService.getById("1");

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test title");
        assertThat(result.get().getCreatedDate()).isNotNull();
    }

    @Test
    void getById_ShouldReturnEmptyIfNotFound() {
        when(noteRepository.findById("999")).thenReturn(Optional.empty());

        Optional<NoteSummaryDto> result = noteService.getById("999");

        assertThat(result).isEmpty();
    }

    @Test
    void getTextById_ShouldReturnText() {
        when(noteRepository.findById("1")).thenReturn(Optional.of(note));

        Optional<NoteTextDto> result = noteService.getTextById("1");

        assertThat(result).isPresent();
        assertThat(result.get().getText()).isEqualTo("This is a test note text");
    }

    @Test
    void listNotes_ShouldReturnPagedNotes_NoTags() {
        Page<Note> notePage = new PageImpl<>(List.of(note));

        when(noteRepository.findAll(any(Pageable.class))).thenReturn(notePage);

        Page<NoteSummaryDto> result = noteService.listNotes(null, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test title");
        verify(noteRepository).findAll(any(Pageable.class));
    }

    @Test
    void listNotes_ShouldReturnPagedNotes_WithTags() {
        Page<Note> notePage = new PageImpl<>(List.of(note));

        when(noteRepository.findByTagsIn(anyCollection(), any(Pageable.class))).thenReturn(notePage);

        Page<NoteSummaryDto> result = noteService.listNotes(List.of(NoteTag.PERSONAL), 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(noteRepository).findByTagsIn(anyCollection(), any(Pageable.class));
    }

    @Test
    void getNoteStatistics_ShouldReturnWordFrequency() {
        String text = "Hello world hello";

        Map<String, Long> stats = noteService.getNoteStatistics(text);

        assertThat(stats).containsEntry("hello", 2L);
        assertThat(stats).containsEntry("world", 1L);
    }

    @Test
    void getNoteStatistics_ShouldReturnEmptyMapForBlankText() {
        Map<String, Long> stats = noteService.getNoteStatistics("   ");
        assertThat(stats).isEmpty();
    }

    @Test
    void getNoteStatistics_ShouldIgnorePunctuation() {
        String text = "Hello, hello! test.";
        Map<String, Long> stats = noteService.getNoteStatistics(text);

        assertThat(stats).containsEntry("hello", 2L);
        assertThat(stats).containsEntry("test", 1L);
    }
}