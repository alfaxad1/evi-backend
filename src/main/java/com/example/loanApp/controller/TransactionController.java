package com.example.loanApp.controller;

import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.PaymentDto;
import com.example.loanApp.dtos.TransactionDto;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.enums.TransactionType;
import com.example.loanApp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping()
    public ResponseEntity<?> getApprovedPayments(
            @RequestParam int userId,
            @RequestParam TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<TransactionDto>> response = transactionService.findTransactionsByStatus(type, userId, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }
}
