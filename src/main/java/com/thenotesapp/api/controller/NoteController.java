package com.thenotesapp.api.controller;

import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/notes")
public class NoteController {

    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        Note createdNote = noteService.create(note);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @Valid @RequestBody Note note) {
        Optional<Note> updatedNote = noteService.update(id, note);
        return updatedNote
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        boolean deleted = noteService.deleteNote(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        Optional<Note> note = noteService.getById(id);
        return note.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<Page<Note>> listNotes(
            @RequestParam(required = false) NoteTag tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Note> notes = noteService.listNotes(tag, page, size);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getNoteStats(@PathVariable String id) {
        Optional<Note> noteOpt = noteService.getById(id);
        if (noteOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Long> stats = noteService.getNoteStatistics(noteOpt.get().getText());
        return ResponseEntity.ok(stats);
    }
}
