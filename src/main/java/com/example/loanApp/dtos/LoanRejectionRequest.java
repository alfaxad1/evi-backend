package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRejectionRequest {
    private Integer loanId;
    private String reason;
}
