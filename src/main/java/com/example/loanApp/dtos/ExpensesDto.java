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
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesDto {
    private Integer id;
    private Integer branchId;
    private String branch;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private ExpenseType expenseType;
    private ExpenseStatus status;
    private String item;
    private BigDecimal unitCost;
    private BigDecimal noOfItems;
    private BigDecimal totalCost;
    private BigDecimal transactionCost;
    private BigDecimal grandTotal;
    private String recipientName;
    private Integer recipientUserId;
    private String receiptUrl;
    private String description;
    private LocalDateTime createdAt;
}
