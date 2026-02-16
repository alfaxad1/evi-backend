package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WeeklyRepaymentResponse {
    private Double weeklyTotal;
    private List<WeeklyStatsProjection> dailyBreakdown;
}
