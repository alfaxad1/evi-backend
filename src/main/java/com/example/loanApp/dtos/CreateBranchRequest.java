package com.example.loanApp.dtos;

import lombok.Data;

@Data
public class CreateBranchRequest {
    private String name;
    private String location;
}
