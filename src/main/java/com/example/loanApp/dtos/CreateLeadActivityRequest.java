package com.example.loanApp.dtos;

import com.example.loanApp.enums.InteractionType;
import com.example.loanApp.enums.LeadStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLeadActivityRequest {
    private LocalDate interactionDate;
    private InteractionType interactionType;
    private String notes;
    private LocalDate nextActionDate;
    /** Optional status change applied together with the activity. */
    private LeadStatus status;
}
