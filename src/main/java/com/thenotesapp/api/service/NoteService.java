package com.thenotesapp.api.service;

import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note create(Note note) {
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (note.getText() == null || note.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }
        note.setCreatedDate(LocalDateTime.now());
        if (note.getTags() != null) {
            for (NoteTag tag : note.getTags()) {
                if (!EnumSet.allOf(NoteTag.class).contains(tag)) {
                    throw new IllegalArgumentException("Invalid tag: " + tag);
                }
            }
        }
        return noteRepository.save(note);
    }

    public Optional<Note> update(String id, Note updatedNote) {
        return noteRepository.findById(id).map(existing -> {
            if (updatedNote.getTitle() == null || updatedNote.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            if (updatedNote.getText() == null || updatedNote.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Text cannot be empty");
            }

            existing.setTitle(updatedNote.getTitle());
            existing.setText(updatedNote.getText());
            existing.setTags(updatedNote.getTags());
            return noteRepository.save(existing);
        });
    }

    public boolean deleteNote(String id) {
        if (!noteRepository.existsById(id)) {
            return false;
        }
        noteRepository.deleteById(id);
        return true;
    }

    public Optional<Note> getById(String id) {
        return noteRepository.findById(id);
    }

    public Page<Note> listNotes(NoteTag tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        if (tag != null) {
            return noteRepository.findByTagsIn(tag, pageable);
        }
        return noteRepository.findAll(pageable);
    }

    public Map<String, Long> getNoteStatistics(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", "")
                .split("\\s+");

        Map<String, Long> frequencyMap = Arrays.stream(words)
                .filter(w -> !w.isBlank())
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
