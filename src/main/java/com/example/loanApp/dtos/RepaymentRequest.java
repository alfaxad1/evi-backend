package com.example.loanApp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RepaymentRequest {
    @JsonProperty("Amount")
    private float Amount;

    @JsonProperty("Narration")
    private String Narration;

    @JsonProperty("TransactionDate")
    private LocalDateTime TransactionDate;
}
