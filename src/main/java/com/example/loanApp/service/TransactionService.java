package com.example.loanApp.service;

import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.TransactionDto;
import com.example.loanApp.enums.TransactionStatus;
import com.example.loanApp.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    GenericResponse<List<TransactionDto>> findTransactionsByStatus(TransactionType type, Integer userId, Pageable pageable);
}
