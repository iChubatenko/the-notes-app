package com.thenotesapp.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thenotesapp.api.dto.CreateNoteDto;
import com.thenotesapp.api.dto.NoteDetailDto;
import com.thenotesapp.api.dto.NoteSummaryDto;
import com.thenotesapp.api.dto.UpdateNoteDto;
import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import com.thenotesapp.api.repository.NoteRepository;
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
    private final ObjectMapper objectMapper;

    public NoteService(NoteRepository noteRepository, ObjectMapper objectMapper) {
        this.noteRepository = noteRepository;
        this.objectMapper = objectMapper;
    }

    public NoteDetailDto create(CreateNoteDto createDto) {
        Note note = objectMapper.convertValue(createDto, Note.class);
        note.setCreatedDate(LocalDateTime.now()
        );
        Note saved = noteRepository.save(note);
        return objectMapper.convertValue(saved, NoteDetailDto.class);
    }

    public NoteDetailDto update(String id, UpdateNoteDto updateDto) {
        Note existing = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (updateDto.getTitle() != null) existing.setTitle(updateDto.getTitle());
        if (updateDto.getText() != null) existing.setText(updateDto.getText());
        if (updateDto.getTags() != null) existing.setTags(updateDto.getTags());

        Note updated = noteRepository.save(existing);
        return objectMapper.convertValue(updated, NoteDetailDto.class);
    }

    public void deleteNote(String id) {
        if (!noteRepository.existsById(id)) {
            throw new NoSuchElementException("Note not found with id: " + id);
        }
        noteRepository.deleteById(id);
    }

    public Optional<NoteDetailDto> getById(String id) {
        return noteRepository.findById(id)
                .map(note -> objectMapper.convertValue(note, NoteDetailDto.class));
    }

    public Page<NoteSummaryDto> listNotes(List<NoteTag> tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Note> notesPage;
        if (tags != null && !tags.isEmpty()) {
            notesPage = noteRepository.findByTagsIn(tags, pageable);
        } else {
            notesPage = noteRepository.findAll(pageable);
        }

        return notesPage.map(note -> objectMapper.convertValue(note, NoteSummaryDto.class));
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