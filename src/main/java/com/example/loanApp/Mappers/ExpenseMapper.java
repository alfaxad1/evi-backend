package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateExpenseRequest;
import com.example.loanApp.dtos.ExpensesDto;
import com.example.loanApp.entities.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "recipientUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "recipientUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CreateExpenseRequest request, @MappingTarget Expense expense);

    @Mapping(source = "branch.name", target = "branch")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "recipientUser.id", target = "recipientUserId")
    @Mapping(target = "grandTotal", ignore = true)
    ExpensesDto toDto(Expense expense);
}
