package com.thenotesapp.api.controller;

import com.thenotesapp.api.dto.*;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/notes")
@Validated
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteDetailDto> createNote(@Valid @RequestBody CreateNoteDto dto) {
        NoteDetailDto createdNote = noteService.create(dto);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDetailDto> updateNote(@PathVariable String id, @Valid @RequestBody CreateNoteDto dto) {
        NoteDetailDto updatedNote = noteService.update(id, dto);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteSummaryDto> getNoteById(@PathVariable String id) {
        return noteService.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id));
    }

    @GetMapping("/{id}/text")
    public ResponseEntity<NoteTextDto> getNoteText(@PathVariable String id) {
        return noteService.getTextById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id));
    }

    @GetMapping
    public ResponseEntity<Page<NoteSummaryDto>> listNotes(
            @RequestParam(required = false) List<NoteTag> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NoteSummaryDto> notes = noteService.listNotes(tags, page, size);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getNoteStatistics(@PathVariable String id) {
        String text = noteService.getTextById(id)
                .map(NoteTextDto::getText)   // витягуємо текст з DTO
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id));

        Map<String, Long> stats = noteService.getNoteStatistics(text);
        return ResponseEntity.ok(stats);
    }
}