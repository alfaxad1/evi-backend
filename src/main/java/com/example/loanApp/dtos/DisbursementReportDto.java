package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementReportDto {
    private Integer id;
    private String customerName;
    private Float principal;
    private Float totalAmount;
    private Float interest;
    private Float processingFee;
    private LocalDate date;
    private String transactionCode;
    private String phoneNumber;
}
