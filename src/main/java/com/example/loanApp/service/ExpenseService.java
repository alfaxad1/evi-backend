package com.example.loanApp.service;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExpenseService {
    void createExpense(CreateExpenseRequest request);

    GenericResponse<List<ExpensesDto>> getExpenses(Pageable pageable);
}
