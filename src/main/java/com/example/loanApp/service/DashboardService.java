package com.example.loanApp.service;

import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.DashboardSummaryDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.enums.LoanStatus;
import com.example.loanApp.enums.RepaymentStatus;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.*;
import com.example.loanApp.utility.LoanHelpers;
import com.example.loanApp.utility.LoanRepayments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.loanApp.enums.ApprovalStatus.approved;
import static com.example.loanApp.enums.ApprovalStatus.pending;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CustomerRepository customerRepository;
    private final UserRepository officerRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

    public DashboardSummaryDto getLoansSummary(int userId) {
        try{
            String role = officerRepository.findRoleById(userId);
            Integer branchId = BranchContext.get();

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = todayStart;

            Long yesterdayCount = customerRepository.countCustomersByDateRange(yesterdayStart, yesterdayEnd);
            Long todayCount = customerRepository.countCustomersByDateRange(todayStart, todayEnd);
            Long customerIncrease = todayCount - yesterdayCount;


            return DashboardSummaryDto.builder()
                    .dueToday(loanRepository.duesCount(userId, LocalDate.now(), List.of(LoanStatus.active, LoanStatus.partially_paid),branchId, role))
                    .dueTomorrow(loanRepository.duesCount(userId, LocalDate.now().plusDays(1), List.of(LoanStatus.active, LoanStatus.partially_paid), branchId, role))
                    .due2To7Days(loanRepository.duesRangeCount(userId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(7), List.of(LoanStatus.active, LoanStatus.partially_paid), branchId, role))
                    .active(loanRepository.loansWithStatusCount(userId, List.of(LoanStatus.active, LoanStatus.partially_paid), branchId, role))
                    .pendingApproval(loanRepository.loansWithApprovalStatusCount(userId,role, branchId, pending))
                    .pendingDisbursement(loanRepository.loansWithStatusCount(userId, List.of(LoanStatus.pending_disbursement), branchId, role))
                    .rejected(loanRepository.loansWithApprovalStatusCount(userId, role, branchId, approved))
                    .defaulted(loanRepository.loansWithStatusCount(userId, List.of(LoanStatus.defaulted), branchId, role))
                    .totalInterest(loanRepository.totalInterest(userId, LoanStatus.active, role, branchId, LocalDate.now().getMonthValue()))
                    .interestPaid(loanRepository.interestPaid(userId, List.of(LoanStatus.active, LoanStatus.paid), role, branchId, LocalDate.now().getMonthValue()))
                    .amountDisbursedToday(loanRepository.dayDisbursedAmount(userId, LoanStatus.active, role, branchId, LocalDate.now()))
                    .amountCollectedToday(repaymentRepository.dailyTotalRepayments(userId, LocalDate.now(), role, RepaymentStatus.paid))
                    .totalMonthlyDisbursement(loanRepository.totalMonthlyDisbursement(userId, LoanStatus.active, role, branchId, LocalDate.now().getMonthValue()))
                    .totalMonthlyCollection(repaymentRepository.totalMonthlyCollection(userId, LocalDateTime.now().getMonthValue(), role, branchId, RepaymentStatus.paid))
                    .interestEarnedToday(loanRepository.interestEarnedToday(userId, LoanStatus.active, role, branchId, LocalDate.now()))
                    //.customersCount(customerRepository.countCustomerByIsActive(true))
                    //.returnCustomers(customerRepository.countReturnCustomers())
                    //.newCustomers(customerRepository.countOneTimeCustomers())
                    .customersIncrease(customerIncrease)
                    .build();

        } catch (Exception e) {
           e.printStackTrace();
            return new DashboardSummaryDto();
        }
    }
}
