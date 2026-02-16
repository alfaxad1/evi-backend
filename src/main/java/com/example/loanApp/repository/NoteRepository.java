package com.example.loanApp.repository;

import com.example.loanApp.entities.Note;
import com.example.loanApp.enums.NoteType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query("select n.id, n.text, n.user.firstName, n.date from Note n " +
            "where n.referenceId = :referenceId " +
            "and n.type = :noteType " +
            "and n.deleted = :deleted " +
            "order by n.id desc")
    List<Tuple> findByReferenceIdAndNoteType(Integer referenceId, NoteType noteType, boolean deleted);
}
