package com.example.loanApp.controller;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.entities.Expense;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/expense")
public class ExpenseController {
    @Autowired
    private ExpenseService expenseService;

    @GetMapping()
    public ResponseEntity<?> getAllExpenses(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page)
    {
        Pageable pageable = PageRequest.of(page, size);
        GenericResponse<List<ExpensesDto>> response = expenseService.getExpenses(pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping()
    public ResponseEntity<?> createExpense(@RequestBody CreateExpenseRequest request) {
        expenseService.createExpense(request);
        return ResponseEntity.ok().body("Expense created successfully");
    }
}
