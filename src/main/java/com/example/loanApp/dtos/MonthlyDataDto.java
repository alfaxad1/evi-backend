package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MonthlyDataDto {
    private String officerName;
    private Long numberOfLoans;
    private Double totalAmount;
    private Double targetAmount;
    private Double deficit;
    private Double percentage;
    private Integer officerId;
}
