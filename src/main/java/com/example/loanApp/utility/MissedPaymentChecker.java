package com.example.loanApp.utility;

import com.example.loanApp.entities.Loan;
import com.example.loanApp.enums.LoanStatus;
import com.example.loanApp.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MissedPaymentChecker {
    private final LoanRepository loanRepository;

    MissedPaymentChecker(LoanRepository loanRepository){
        this.loanRepository = loanRepository;
    }

    @Transactional
    public List<Loan> checkMissedPayments (){
        List<Loan> loans = loanRepository.findByNextInstallmentDateBeforeAndLoanStatusAndLoanCurrentStatus(LocalDate.now(), LoanStatus.active, LoanStatus.paid);
        loans.forEach(loan ->{
            loan.setArrears(loan.getArrears() + loan.getInstallmentAmount());
            loan.setNextInstallmentDate(LoanCalculations.setNextDueDate(loan.getNextInstallmentDate(), loan.getInstallmentType()));
            loanRepository.save(loan);
        });
        return loans;
    }
}
