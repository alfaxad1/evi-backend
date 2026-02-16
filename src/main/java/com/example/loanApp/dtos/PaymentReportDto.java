package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReportDto {
    private Integer id;
    private String customerName;
    private Float amount;
    private LocalDateTime paymentDate;
    private String officerName;
    private String phoneNumber;
    private String transactionCode;
}
