package com.example.loanApp.service;

import com.example.loanApp.Mappers.ExpenseMapper;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.Branch;
import com.example.loanApp.entities.Expense;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.BranchRepository;
import com.example.loanApp.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseMapper expenseMapper;
    private final ExpenseRepository expenseRepository;
    private final BranchRepository branchRepository;



    @Override
    public void createExpense(CreateExpenseRequest request) {
        try {
            Branch branch = branchRepository.findById(request.getBranchId()).orElseThrow(() -> new RuntimeException("Branch not found"));
            Expense expense = expenseMapper.toEntity(request);
            expense.setBranch(branch);
            expenseRepository.save(expense);
        }catch (Exception e){
            log.error("Exception caught while trying to create expense");
            throw new RuntimeException("Error while trying to create expense");
        }
    }

    @Override
    public GenericResponse<List<ExpensesDto>> getExpenses(Pageable pageable) {
        try {
            Page<Expense> expenses = expenseRepository.findAll(pageable);

            Page<ExpensesDto> expensesDto = expenses.map(expenseMapper::toDto);

            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(expenses.getNumber())
                    .totalElements(expenses.getTotalElements())
                    .totalPages(expenses.getTotalPages())
                    .limit(expenses.getSize())
                    .build();

            return GenericResponse.<List<ExpensesDto>>builder()
                    .data(expensesDto.getContent())
                    .message("expenses fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();

        }catch (Exception e){
            log.error("Exception caught while trying to get expenses");
            throw new RuntimeException("Error while trying to get expenses");
        }
    }
}
