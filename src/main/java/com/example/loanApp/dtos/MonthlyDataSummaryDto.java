package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyDataSummaryDto {
    private Long numberOfLoans;
    private Double totalAmount;
    private Double targetAmount;
    private Double deficit;
    private Double percentage;
}
