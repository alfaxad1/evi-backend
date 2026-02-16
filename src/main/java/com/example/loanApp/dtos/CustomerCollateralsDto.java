package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCollateralsDto {
    private Integer id;
    private String itemName;
    private Integer itemCount;
    private String additionalDetails;
}
