package com.example.loanApp.dtos;

import com.example.loanApp.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Integer id;
    private String customerName;
    private String transactionCode;
    private Float amount;
    private LocalDateTime timestamp;
    private TransactionStatus status;

}
