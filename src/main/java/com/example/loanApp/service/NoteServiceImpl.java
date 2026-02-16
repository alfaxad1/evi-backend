package com.example.loanApp.service;

import com.example.loanApp.dtos.AddNoteRequest;
import com.example.loanApp.dtos.EditNoteRequest;
import com.example.loanApp.dtos.NoteDto;
import com.example.loanApp.entities.Note;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.NoteType;
import com.example.loanApp.repository.LoanRepository;
import com.example.loanApp.repository.NoteRepository;
import com.example.loanApp.repository.RepaymentRepository;
import com.example.loanApp.repository.UserRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;

    @Override
    public void addNote(AddNoteRequest noteRequest) {
        try {
            log.info("Adding note to database for userId {} ",  noteRequest.getUserId());
            User user = userRepository.findById(noteRequest.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

            Note note = Note.builder()
                    .referenceId(noteRequest.getReferenceId())
                    .text(noteRequest.getNote())
                    .type(noteRequest.getNoteType())
                    .date(LocalDateTime.now())
                    .user(user)
                    .build();
            noteRepository.save(note);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<NoteDto> getNotes(Integer referenceId, NoteType noteType) {
        try {
            List<NoteDto> notesList = new ArrayList<>();

            if(noteType == NoteType.loan){
                loanRepository.findById(referenceId).orElseThrow(()-> new RuntimeException("no loan found with id " + referenceId));
            }else if(noteType == NoteType.repayment){
                repaymentRepository.findById(referenceId).orElseThrow(()-> new RuntimeException("no payment found with id " + referenceId));
            }

            List<Tuple> notes = noteRepository.findByReferenceIdAndNoteType(referenceId, noteType, false);
            for (Tuple note : notes) {
                NoteDto dto = NoteDto.builder()
                        .id(note.get(0, Integer.class))
                        .text(note.get(1, String.class))
                        .createdBy(note.get(2, String.class))
                        .date(note.get(3, LocalDateTime.class))
                        .build();
                notesList.add(dto);
            }

            return notesList;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteNote(Integer id) {
        Note note = noteRepository.findById(id).orElseThrow(()-> new RuntimeException("note not found with id " + id));
        note.setDeleted(true);
        noteRepository.save(note);
    }

    @Override
    public void editNote(Integer id, EditNoteRequest request) {
        Note note = noteRepository.findById(id).orElseThrow(()-> new RuntimeException("note not found with id " + id));
        note.setText(request.getNote());
        noteRepository.save(note);
    }
}
