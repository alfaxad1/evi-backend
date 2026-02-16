package com.example.loanApp.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerCollateralRequest {
    //@NotNull(message = "Item cannot be null")
    private String itemName;

    private Integer itemCount;
    private String additionalDetails;
}
