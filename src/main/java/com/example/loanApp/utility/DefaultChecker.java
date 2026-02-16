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
public class DefaultChecker {
    private final LoanRepository loanRepository;
    DefaultChecker(LoanRepository loanRepository){
        this.loanRepository = loanRepository;
    }

    @Transactional
    public List<Loan> checkLoanDefaults(){
        try {
            List<Loan> loans = loanRepository.findByDueDateBeforeAndLoanStatusNot(LocalDate.now(), LoanStatus.defaulted);
            if (!loans.isEmpty()) {
                loans.forEach(loan -> {
                    try {
                        loan.setLoanStatus(LoanStatus.defaulted);
                        loan.setLoanCurrentStatus(LoanStatus.defaulted);
                        loanRepository.save(loan);
                    } catch (Exception e) {
                        System.err.println("Failed to update loan" + loan.getId());
                        throw new RuntimeException(e.getMessage());
                    }
                });
            }
            return loans;
        } catch (Exception e) {
            System.err.println("No defaults found");
            throw new RuntimeException(e.getMessage());
        }

    }

}
