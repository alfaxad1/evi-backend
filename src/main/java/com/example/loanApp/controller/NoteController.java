package com.example.loanApp.controller;

import com.example.loanApp.dtos.AddNoteRequest;
import com.example.loanApp.dtos.EditNoteRequest;
import com.example.loanApp.dtos.NoteDto;
import com.example.loanApp.enums.NoteType;
import com.example.loanApp.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping()
    public ResponseEntity<?> createNote(@RequestBody AddNoteRequest noteRequest) {
        noteService.addNote(noteRequest);
        return ResponseEntity.ok().body("Note added");
    }

    @GetMapping()
    public ResponseEntity<List<NoteDto>> getNotes(@RequestParam Integer referenceId, @RequestParam NoteType noteType) {
        return ResponseEntity.ok().body(noteService.getNotes(referenceId, noteType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Integer id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok().body("Note deleted");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Integer id, @RequestBody EditNoteRequest request) {
        noteService.editNote(id, request);
        return ResponseEntity.ok().body("Note updated");
    }
}
