package com.example.loanApp.dtos;

import com.example.loanApp.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto {
    private Integer id;
    private String name;
    private String phoneNumber;
    private LeadStatus status;
    private String source;
    private String location;
    private Integer branchId;
    private String branch;
    private Integer assignedOfficerId;
    private String assignedOfficer;
    private LocalDate nextActionDate;
    private LocalDate lastInteractionDate;
    private Integer activityCount;
    /** true when nextActionDate is today or in the past and the lead is still open. */
    private Boolean followUpDue;
    private Integer daysOverdue;
    private Integer convertedCustomerId;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
    private List<LeadActivityDto> activities;
}
