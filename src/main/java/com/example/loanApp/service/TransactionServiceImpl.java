package com.example.loanApp.service;

import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.PendingLoansDto;
import com.example.loanApp.dtos.TransactionDto;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.enums.TransactionStatus;
import com.example.loanApp.enums.TransactionType;
import com.example.loanApp.repository.TransactionRepository;
import com.example.loanApp.repository.UserRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository officerRepository;

    @Override
    public GenericResponse<List<TransactionDto>> findTransactionsByStatus(TransactionType type, Integer userId, Pageable pageable) {
        List<TransactionDto> transactionDtoList = new ArrayList<>();
        try {
            String role = getUserRole(userId);
            Integer branchId = BranchContext.get();

            Page<Tuple> transactions = transactionRepository.findTransactionsByStatus(type, userId, role, branchId, pageable);
            for (Tuple transaction : transactions) {
                TransactionDto dto = TransactionDto.builder()
                        .id(transaction.get(0, Integer.class))
                        .customerName(transaction.get(1, String.class))
                        .transactionCode(transaction.get(2, String.class))
                        .amount(transaction.get(3, Float.class))
                        .timestamp(transaction.get(4, LocalDateTime.class))
                        .status(transaction.get(5, TransactionStatus.class))
                        .build();
                transactionDtoList.add(dto);
            }
            return GenericResponse.<List<TransactionDto>>builder()
                    .data(transactionDtoList)
                    .message("loans fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .build();
        }catch (Exception e){
            return GenericResponse.<List<TransactionDto>>builder()
                    .message("Error retrieving transactions: " + e.getMessage())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    private String getUserRole(Integer userId){
        return officerRepository.findRoleById(userId);
    }
}

