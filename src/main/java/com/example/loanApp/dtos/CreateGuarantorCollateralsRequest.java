package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuarantorCollateralsRequest {
    private String itemName;
    private Integer itemCount;
    private String additionalDetails;
}
