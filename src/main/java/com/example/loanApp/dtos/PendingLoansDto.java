package com.example.loanApp.dtos;

import com.example.loanApp.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingLoansDto {
    private Integer loanId;
    private String customerName;
    private float monthlyIncome;
    private String phoneNumber;
    private String purpose;
    private ApprovalStatus status;
    private String product;
    private String officer;
    private LocalDateTime applicationDate;
    private String rejectionReason;
    private LocalDate rejectionDate;
    private Float appliedAmount;
}
