package com.example.loanApp.dtos;

import lombok.Data;

@Data
public class LoanDisbursermentRequest {
    private Integer loanId;
    private Integer userId;
    private Float amount;
    private String transactionCode;
}
