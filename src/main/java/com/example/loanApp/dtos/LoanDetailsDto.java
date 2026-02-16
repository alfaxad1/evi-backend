package com.example.loanApp.dtos;

import com.example.loanApp.enums.InstallmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailsDto {
    private Integer loanId;
    private String customerName;
    private Float principal;
    private Float processingFee;
    private Float interest;
    private Float totalAmount;
    private Float paidAmount;
    private Float balance;
    private Float monthlyIncome;
    private Float installmentAmount;
    private Float arrears;
    private InstallmentType installmentType;
    private String phoneNumber;
    private String purpose;
    private String loanStatus;
    private String loanCurrentStatus;
    private String product;
    private String officer;
    private LocalDate dueDate;
    private Integer daysRemaining;
    private LocalDate disburseDate;
    private LocalDate defaultDate;
    private LocalDate applicationDate;
    private Float appliedAmount;
    private LocalDate approvalDate;
    private LocalDateTime updatedAt;
    private List<PaymentDto> payments;
}
