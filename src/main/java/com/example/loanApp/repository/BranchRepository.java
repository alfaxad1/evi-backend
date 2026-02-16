package com.example.loanApp.repository;

import com.example.loanApp.entities.Branch;
import com.example.loanApp.enums.BranchStatus;
import com.example.loanApp.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
    List<Branch> findByStatus(BranchStatus status);
}
