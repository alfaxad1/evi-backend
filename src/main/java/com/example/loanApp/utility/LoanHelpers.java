package com.example.loanApp.utility;

import com.example.loanApp.enums.LoanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanHelpers {

    public String getLoanStatusDescription(LoanStatus status) {
        if (status == null) {
            return "Unknown Status";
        }
        return switch (status) {
            case active -> "Active";
            case partially_paid -> "Partial";
            case paid -> "Paid";
            case defaulted -> "Defaulted";
            case pending_disbursement -> "Pending Disbursement";
            case rolled_over -> "Rolled Over";
            default -> "Unknown Status";
        };
    }
}
