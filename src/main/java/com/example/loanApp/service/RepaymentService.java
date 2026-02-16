package com.example.loanApp.service;

import com.example.loanApp.dtos.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RepaymentService {
    void repayment(RepaymentRequest request);

    GenericResponse<List<PaymentDto>> getPendingPayments(String search,Pageable pageable);

    GenericResponse<List<PaymentDto>> getApprovedPayments(int userId, String search, Pageable pageable);

    void resolvePayment(int loanId, int repaymentId);

    WeeklyRepaymentResponse getWeeklyStats(String date, Integer userId);

    void manualRepayment(Float amount, LocalDateTime date, String transactionCode, Integer loanId);

}
