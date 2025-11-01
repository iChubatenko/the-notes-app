package com.thenotesapp.api.dto;

import com.thenotesapp.api.model.NoteTag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDetailDto {

    //kept id field just for simplified testing
    private String id;
    @NotBlank(message = "Title cannot be empty")
    @Indexed
    private String title;
    @NotBlank(message = "Text cannot be empty")
    private String text;
    private List<NoteTag> tags;
    private LocalDateTime createdDate;
}
