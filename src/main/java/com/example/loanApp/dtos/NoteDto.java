package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoteDto {
    private Integer id;
    private String text;
    private LocalDateTime date;
    private String createdBy;
}
