package com.example.loanApp.utility;

import com.example.loanApp.enums.InstallmentType;
import com.example.loanApp.enums.LoanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanCalculations {
    public static float processingFee(float amount){
        return (amount * 3) / 100;
    }

    public static float loanInterest(float amount, float interestRate){
        return amount * interestRate / 100;
    }

    public static float totalAmount(float amount, float loanInterest){
        return amount + loanInterest;
    }

    public static float calculateRepaymentAmount(float amount){
        if(amount > 200)
            return amount = Math.round(amount + (amount * 0.55f) / 100f);
        return amount;
    }

    public static float reverseRepaymentAmount(float finalAmount){
        if(finalAmount > 200)
            return finalAmount / 1.0055f;
        return finalAmount;
    }

    public static float calculateArrears(float amount, float arrear, float installmentAmount){
        if(arrear < amount)
            return  arrear += installmentAmount - amount;
        return arrear -= amount - installmentAmount;
    }

    public static LocalDate setNextDueDate(LocalDate nextDueDate, InstallmentType installmentType){
        if(installmentType.equals(InstallmentType.daily))
            return nextDueDate.plusDays(1);
        else
            return nextDueDate.plusDays(7);
    }

    public static float calculateBalance(float balance, float amount, float total){
        if(balance == 0)
            return total - amount;
        return balance - amount;
    }
    public static LoanStatus changeLoanStatus(float balance, LocalDate date, float paidAmount, float totalAmount){
        if(balance <= 0)
            return LoanStatus.paid;
        else if (LocalDate.now().isAfter(date))
            return LoanStatus.defaulted;
        else
            return LoanStatus.active;
    }

    public static LoanStatus changeLoanCurrentStatus(float balance, LocalDate date, float paidAmount,  float totalAmount){
        if(balance <= 0)
            return LoanStatus.paid;
        else if (paidAmount > 0 && paidAmount < totalAmount)
            return LoanStatus.partially_paid;
        else if (LocalDate.now().isAfter(date))
            return LoanStatus.defaulted;
        else
            return LoanStatus.active;
    }
}
