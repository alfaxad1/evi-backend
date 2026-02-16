package com.example.loanApp.service;

import com.example.loanApp.dtos.*;
import com.example.loanApp.enums.ApprovalStatus;
import com.example.loanApp.enums.LoanStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LoanService {
    void applyLoan(LoanApplicationRequest loanApplicationRequest);

    void approveLoan(Integer loanId);

    void disburseLoan(LoanDisbursermentRequest loanDisbursermentRequest);

    GenericResponse<List<MonthlyDataDto>> getMonthlyData(Long month, Long Year, String type);

    //MonthlyDataSummaryDto getMonthlyDataSummary(Long month, Long year, String type);

    GenericResponse<List<PendingLoansDto>> getLoansWithApprovalStatus(String search, ApprovalStatus approvalStatus, int userId, Pageable pageable);

    GenericResponse<List<LoansDto>> getLoansWithStatus(String search, List<LoanStatus> status, int userId, Pageable pageable);

    GenericResponse<List<DueLoansDto>> getDueLoansByDay(LocalDate day, int userId,  String search, Pageable pageable);

    GenericResponse<List<DueLoansDto>> getDueLoansByRange(LocalDate fromDate, LocalDate toDate, int userId,  String search, Pageable pageable);

    GenericResponse<DashboardSummaryDto> getLoansSummary(int userId);

    GenericResponse<List<RolledOverLoansDto>> getRolledOverLoans(Integer userId, String search, Pageable pageable);

    void rollOverLoan(int loanId, float principal);

    void rejectLoan(@Valid LoanRejectionRequest loanRejectionRequest);

    GenericResponse<LoanDetailsDto> getLoanWithId(int id);

    List<MonthlyTrendResponse> getRolling12MonthTrends(Integer userId);

    List<LoansDto> getAllLoans(
            String search,
            List<LoanStatus> status,
            Integer userId,
            Integer branchId,
            LocalDate dueDay,
            LocalDate dueFrom,
            LocalDate dueTo,
            LocalDate appliedDate,
            LocalDate disbursedDate,
            Integer customerId,
            Pageable pageable);

    void clearLoan(int id);

    List<PaymentReportDto> getAllCollections(Integer officerId, LocalDate startDate, LocalDate endDate);

    List<DisbursementReportDto> getAllDisbursements(Integer officerId, LocalDate startDate, LocalDate endDate);
}
