package com.example.loanApp.controller;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpenseSummaryDto;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/v1/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<?> getAllExpenses(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) ExpenseType expenseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        GenericResponse<List<ExpensesDto>> response =
                expenseService.getExpenses(pageable, category, status, expenseType, from, to, search);

        if (response != null && ResponseStatusEnum.SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDto> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        return ResponseEntity.ok(expenseService.getSummary(month));
    }

    @PostMapping
    public ResponseEntity<ExpensesDto> createExpense(@RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.createExpense(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpensesDto> updateExpense(@PathVariable Integer id,
                                                     @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExpensesDto> updateStatus(@PathVariable Integer id,
                                                    @RequestParam ExpenseStatus status) {
        return ResponseEntity.ok(expenseService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Integer id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().body("Expense deleted successfully");
    }
}
