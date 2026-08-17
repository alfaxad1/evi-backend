package com.example.loanApp.dtos;

import com.example.loanApp.enums.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivityDto {
    private Integer id;
    private Integer leadId;
    private LocalDate interactionDate;
    private InteractionType interactionType;
    private String notes;
    private LocalDate nextActionDate;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
