package com.example.loanApp.dtos;

import com.example.loanApp.enums.RepaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Integer id;
    private Integer loanId;
    private String customerName;
    private float paymentAmount;
    private LocalDateTime paymentDate;
    private RepaymentStatus paymentStatus;
    private String transactionCode;
    private String paymentName;
    private String loanStatus;
    private float loanAmount;
}
