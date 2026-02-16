package com.example.loanApp.dtos;

import com.example.loanApp.entities.Expense;
import com.example.loanApp.enums.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {
    private Integer branchId;
    private String item;
    private Float unitCost;
    private ExpenseType expenseType;
    private BigDecimal noOfItems;
    private Float totalCost;
    private String description;
}
