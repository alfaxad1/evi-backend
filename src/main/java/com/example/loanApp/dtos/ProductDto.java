package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Integer id;
    private String productName;
    private String description;
    private float minAmount;
    private float maxAmount;
    private float interestRate;
    private Integer duration;
}
