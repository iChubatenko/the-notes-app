package com.thenotesapp.api.dto;

import com.thenotesapp.api.model.NoteTag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateNoteDto {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    @NotBlank(message = "Text cannot be empty")
    private String text;
    private List<NoteTag> tags;
}
