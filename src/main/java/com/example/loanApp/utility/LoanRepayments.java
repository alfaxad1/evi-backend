package com.example.loanApp.utility;

import com.example.loanApp.dtos.PaymentDto;
import com.example.loanApp.enums.LoanStatus;
import com.example.loanApp.enums.RepaymentStatus;
import com.example.loanApp.repository.RepaymentRepository;
import com.example.loanApp.service.LoanService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanRepayments {
    private final RepaymentRepository repaymentRepository;
    private final LoanHelpers loanHelpers;

    public List<PaymentDto> getLoanRepayments(int loanId) {
        List<PaymentDto> paymentDtoList = new ArrayList<>();

        try {
            List<Tuple> payments = repaymentRepository.getLoanRepayments(loanId, RepaymentStatus.paid);
            for(Tuple payment : payments){
                PaymentDto dto = PaymentDto.builder()
                        .loanId(payment.get(0, Integer.class))
                        .customerName(payment.get(1, String.class))
                        .paymentDate(payment.get(2, LocalDateTime.class))
                        .paymentStatus(payment.get(3, RepaymentStatus.class))
                        .transactionCode(payment.get(4, String.class))
                        .paymentName(payment.get(5, String.class))
                        .loanStatus(loanHelpers.getLoanStatusDescription(payment.get(6, LoanStatus.class)))
                        .loanAmount(payment.get(7, Float.class))
                        .paymentAmount(payment.get(8, Float.class))
                        .build();
                paymentDtoList.add(dto);
            }
            return paymentDtoList;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching payments");
        }
    }


}
