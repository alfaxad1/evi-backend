package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSummaryDto {
    private long openLeads;
    private long dueToday;
    private long overdue;
    private long convertedThisMonth;
    private long deadLeads;
    private long totalLeads;
    private double conversionRate;
}
