package com.example.loanApp.service;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpenseSummaryDto;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpensesDto createExpense(CreateExpenseRequest request);

    ExpensesDto updateExpense(Integer id, CreateExpenseRequest request);

    ExpensesDto updateStatus(Integer id, ExpenseStatus status);

    void deleteExpense(Integer id);

    GenericResponse<List<ExpensesDto>> getExpenses(Pageable pageable,
                                                   ExpenseCategory category,
                                                   ExpenseStatus status,
                                                   ExpenseType expenseType,
                                                   LocalDate from,
                                                   LocalDate to,
                                                   String search);

    ExpenseSummaryDto getSummary(LocalDate month);
}
