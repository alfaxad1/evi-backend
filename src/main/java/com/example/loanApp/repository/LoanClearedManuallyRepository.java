package com.example.loanApp.repository;

import com.example.loanApp.entities.LoanClearedManually;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanClearedManuallyRepository extends JpaRepository<LoanClearedManually, Long> {
}
