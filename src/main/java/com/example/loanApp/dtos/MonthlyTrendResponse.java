package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendResponse {
    private Integer year;
    private Integer month;
    private String monthName; // e.g., "Jan"
    private BigDecimal collections;
    private BigDecimal disbursements;

    public static String getMonthAbbreviation(int month) {
        return Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }
}
