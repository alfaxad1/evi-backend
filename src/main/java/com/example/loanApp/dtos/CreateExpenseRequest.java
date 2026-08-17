package com.example.loanApp.dtos;

import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {
    private Integer id;
    private Integer branchId;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private ExpenseType expenseType;
    private ExpenseStatus status;
    private String item;
    private BigDecimal unitCost;
    private BigDecimal noOfItems;
    private BigDecimal totalCost;
    private BigDecimal transactionCost;
    private String recipientName;
    private Integer recipientUserId;
    private String receiptUrl;
    private String description;
}
