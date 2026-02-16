package com.example.loanApp.service;

import com.example.loanApp.entities.Branch;
import com.example.loanApp.enums.BranchStatus;
import com.example.loanApp.enums.LoanStatus;
import com.example.loanApp.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;

    public List<Branch> getBranches(){
        try {
            return branchRepository.findByStatus(BranchStatus.active);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addEditBranch(Branch branch) {
        try {

            if (branch.getId() == null) {
                Branch br = new Branch();
                br.setName(branch.getName());
                br.setLocation(branch.getLocation());
                br.setStatus(BranchStatus.active);
                branchRepository.save(br);
            } else {
                Branch branch1 = branchRepository.findById(branch.getId()).get();
                branch1.setName(branch.getName());
                branch1.setLocation(branch.getLocation());
                branch1.setStatus(branch.getStatus());
                branchRepository.save(branch1);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
