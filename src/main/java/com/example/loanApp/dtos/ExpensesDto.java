package com.example.loanApp.dtos;

import com.example.loanApp.enums.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesDto {
    private Integer id;
    private String branch;
    private String item;
    private Float unitCost;
    private ExpenseType expenseType;
    private BigDecimal noOfItems;
    private Float totalCost;
    private String description;
}
