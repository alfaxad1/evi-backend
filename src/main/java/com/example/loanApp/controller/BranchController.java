package com.example.loanApp.controller;

import com.example.loanApp.entities.Branch;
import com.example.loanApp.service.BranchService;
import com.example.loanApp.utility.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/branch")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @GetMapping()
    public ResponseEntity<List<Branch>> getBranches(){
        return ResponseEntity.ok().body(branchService.getBranches());
    }

    @PostMapping()
    public ResponseEntity<?> addEditBranch(@RequestBody Branch branch){
        branchService.addEditBranch(branch);
        return ResponseHandler.responseBuilder("branch added successfully", HttpStatus.CREATED, null);
    }
}
