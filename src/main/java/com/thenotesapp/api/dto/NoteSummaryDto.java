package com.thenotesapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NoteSummaryDto {

    //kept id field just for simplified testing
    private String id;
    @NotBlank(message = "Title cannot be empty")
    @Indexed
    private String title;
    private LocalDateTime createdDate;
}
