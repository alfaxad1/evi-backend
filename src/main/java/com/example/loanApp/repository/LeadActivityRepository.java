package com.example.loanApp.repository;

import com.example.loanApp.entities.LeadActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadActivityRepository extends JpaRepository<LeadActivity, Integer> {

    List<LeadActivity> findByLeadIdOrderByInteractionDateDescIdDesc(Integer leadId);

    long countByLeadId(Integer leadId);
}
