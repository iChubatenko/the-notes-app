package com.thenotesapp.api.repository;

import com.thenotesapp.api.model.Note;
import com.thenotesapp.api.model.NoteTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    Page<Note> findByTagsIn(NoteTag tags, Pageable pageable);
}
