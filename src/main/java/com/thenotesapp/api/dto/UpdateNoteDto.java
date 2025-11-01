package com.thenotesapp.api.dto;

import com.thenotesapp.api.model.NoteTag;
import lombok.Data;

import java.util.List;

@Data
public class UpdateNoteDto {

    private String title;
    private String text;
    private List<NoteTag> tags;
}
