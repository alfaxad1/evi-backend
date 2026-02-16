package com.example.loanApp.service;

import com.example.loanApp.dtos.AddNoteRequest;
import com.example.loanApp.dtos.EditNoteRequest;
import com.example.loanApp.dtos.NoteDto;
import com.example.loanApp.enums.NoteType;

import java.util.List;

public interface NoteService {
    void addNote(AddNoteRequest noteRequest);

    List<NoteDto> getNotes(Integer referenceId, NoteType noteType);

    void deleteNote(Integer id);

    void editNote(Integer id, EditNoteRequest request);
}
