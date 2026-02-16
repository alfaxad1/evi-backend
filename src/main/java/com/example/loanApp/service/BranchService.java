package com.example.loanApp.service;

import com.example.loanApp.entities.Branch;
import com.example.loanApp.enums.LoanStatus;

import java.util.List;

public interface BranchService {
    List<Branch> getBranches();

    void addEditBranch(Branch branch);
}
