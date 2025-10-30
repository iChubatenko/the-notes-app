package com.thenotesapp.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {
    @Id
    private String id;
    @NotBlank(message = "Title cannot be empty")
    @Indexed
    private String title;
    @NotBlank(message = "Text cannot be empty")
    private String text;
    private LocalDateTime createdDate;
    private List<NoteTag> tags;
}
