package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolledOverLoansDto {
    private Integer preLoanId;
    private Integer currLoanId;
    private float balanceAtRollover;
    private float currBalance;
    private float prevPrincipal;
    private float currPrincipal;
    private float prevInterest;
    private float currInterest;
    private float prevTotalAmount;
    private float currTotalAmount;
    private LocalDate prevDue;
    private LocalDate currDue;
    private LocalDate rollOverDate;
    private String customerName;
}
