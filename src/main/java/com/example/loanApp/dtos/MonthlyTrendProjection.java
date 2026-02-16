package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyTrendProjection {
    private Integer year;
    private Integer month;
    private BigDecimal sum;
}
