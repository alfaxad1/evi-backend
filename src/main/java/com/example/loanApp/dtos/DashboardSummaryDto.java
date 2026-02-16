package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {
    private Long dueToday;
    private Long dueTomorrow;
    private Long due2To7Days;
    private Long active;
    private Long pendingApproval;
    private Long pendingDisbursement;
    private Long defaulted;
    private Long rejected;
    private float totalInterest;
    private float interestEarnedToday;
    private float interestPaid;
    private float amountDisbursedToday;
    private float amountCollectedToday;
    private float totalMonthlyCollection;
    private float totalMonthlyDisbursement;
    private Long customersCount;
    private Long newCustomers;
    private Long customersIncrease;
    private Long returnCustomers;
    private Long newActiveLoansCount;
}
