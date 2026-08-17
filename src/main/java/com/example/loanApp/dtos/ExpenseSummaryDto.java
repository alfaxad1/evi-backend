package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryDto {
    private BigDecimal monthToDateTotal;
    private BigDecimal previousMonthTotal;
    private BigDecimal pendingReimbursementTotal;
    private long pendingReimbursementCount;
    private BigDecimal transactionCostTotal;
    private long monthToDateCount;
    private List<CategoryTotal> byCategory;
    private List<DailyTotal> dailyTrend;
    private List<ExpensesDto> recent;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTotal {
        private String category;
        private BigDecimal total;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTotal {
        private String date;
        private BigDecimal total;
    }
}
