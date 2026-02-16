package com.example.loanApp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoanApprovalRequest {
//    @NotNull(message = "Amount is required")
//    @Schema(
//            description = "Loan amount to be approved",
//            example = "10000"
//    )
    private Integer loanId;
    private float amount;
}
