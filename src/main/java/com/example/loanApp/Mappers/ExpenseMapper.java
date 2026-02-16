package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.dtos.PaymentDto;
import com.example.loanApp.entities.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    @Mapping(target = "branch", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    @Mapping(source = "branch.name", target = "branch")
    ExpensesDto toDto(Expense expense);

}
