package com.thenotesapp.api.dto;

import com.thenotesapp.api.model.NoteTag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNoteDto {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    @NotBlank(message = "Text cannot be empty")
    private String text;
    private List<NoteTag> tags;
}
