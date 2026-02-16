package com.example.loanApp.dtos;

import com.example.loanApp.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DueLoansDto {
    private Integer loanId;
    private String customerName;
    private String phoneNumber;
    private String productName;
    private float principal;
    private float interest;
    private float totalAmount;
    private float paidAmount;
    private float balance;
    private LocalDate nextInstallmentDate;
    private LocalDate dueDate;
    private String loanStatus;
    private String loanCurrentStatus;
    private Integer daysRemaining;
}
