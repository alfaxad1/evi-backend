package com.example.loanApp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.example.loanApp.enums.InstallmentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer applying for the loan", example = "123")
    private Integer customerId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the loan product", example = "456")
    private Integer productId;

    @NotNull(message = "Officer ID is required")
    @Schema(description = "ID of the loan officer processing the application", example = "789")
    private Integer officerId;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be positive")
    @Schema(description = "Loan principal amount", example = "50000.00")
    private Float principal;

    @NotBlank(message = "Loan purpose is required")
    @Size(min = 10, max = 500, message = "Purpose must be between 10 and 500 characters")
    @Schema(description = "Purpose of the loan", example = "Home renovation and improvement")
    private String purpose;

    @NotNull(message = "Installment type is required")
    @Schema(description = "Type of installment plan", example = "MONTHLY")
    private InstallmentType installmentType;

}
