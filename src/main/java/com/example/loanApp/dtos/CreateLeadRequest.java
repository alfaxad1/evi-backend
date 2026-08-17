package com.example.loanApp.dtos;

import com.example.loanApp.enums.InteractionType;
import com.example.loanApp.enums.LeadStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLeadRequest {
    private String name;
    private String phoneNumber;
    private LeadStatus status;
    private String source;
    private String location;
    private Integer branchId;
    private Integer assignedOfficerId;

    /** Optional first interaction captured together with the lead. */
    private LocalDate interactionDate;
    private InteractionType interactionType;
    private String notes;
    private LocalDate nextActionDate;
}
