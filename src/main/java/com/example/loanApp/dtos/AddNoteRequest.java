package com.example.loanApp.dtos;

import com.example.loanApp.enums.NoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNoteRequest {
    private String note;
    private Integer userId;
    private Integer referenceId;
    private NoteType noteType;
}
