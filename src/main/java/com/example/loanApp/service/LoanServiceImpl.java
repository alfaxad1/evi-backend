package com.example.loanApp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.*;
import com.example.loanApp.entities.LoanClearedManually;
import com.example.loanApp.enums.*;
import com.example.loanApp.repository.*;
import com.example.loanApp.utility.LoanHelpers;
import com.example.loanApp.utility.LoanRepayments;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loanApp.exceptions.ResourceNotFoundException;

import com.example.loanApp.utility.LoanCalculations;

import jakarta.persistence.Tuple;

import static com.example.loanApp.enums.ApprovalStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository officerRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final RepaymentRepository repaymentRepository;
    private final  RolledOverLoansRepository rolledOverLoansRepository;
    private final LoanRepayments loanRepayments;
    private final LoanHelpers loanHelpers;
    private final DashboardSseService dashboardSseService;
    private final LoanClearedManuallyRepository clearedManuallyRepository;
    private final UserDetailsService userDetailsService;


    @Override
    public void applyLoan(LoanApplicationRequest loanApplicationRequest) {
        Loan loan = new Loan();

        Customer customer = customerRepository.findById(loanApplicationRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("No customer found with id: "+loanApplicationRequest.getCustomerId()));
        LoanProduct product = productRepository.findById(loanApplicationRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: "+loanApplicationRequest.getProductId()));
        User user = officerRepository.findById(loanApplicationRequest.getOfficerId())
                .orElseThrow(() -> new ResourceNotFoundException("No officer found with id: "+loanApplicationRequest.getOfficerId()));

        if(loanRepository.existsByCustomerAndLoanStatusNotIn(customer, List.of(LoanStatus.paid, LoanStatus.rolled_over))){
            throw new IllegalArgumentException(
                    "Customer has an active loan"
            );
        }

        if( loanApplicationRequest.getPrincipal() > product.getMaxAmount() || loanApplicationRequest.getPrincipal() < product.getMinAmount())
            throw new RuntimeException("Principal is too big or small");

        loan.setCustomer(customer);
        loan.setProduct(product);
        loan.setUser(user);
        loan.setBranch(user.getBranch());
        loan.setAppliedAmount(loanApplicationRequest.getPrincipal());
        loan.setPurpose(loanApplicationRequest.getPurpose());
        loan.setApprovalStatus(ApprovalStatus.pending);

        loan.setInstallmentType(loanApplicationRequest.getInstallmentType());
        loan.setApplicationDate(LocalDate.now());

        loanRepository.save(loan);

        Integer officerId = loan.getUser().getId();
        dashboardSseService.send(officerId);
    }

    @Override
    public void approveLoan(Integer loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("No loan with id " + loanId + " found"));

        loan.setApprovalStatus(approved);
        loan.setApprovalDate(LocalDate.now());
        loan.setLoanStatus(LoanStatus.pending_disbursement);

        loanRepository.save(loan);

        Integer officerId = loan.getUser().getId();
        dashboardSseService.send(officerId);
    }

    @Override
    public void disburseLoan(LoanDisbursermentRequest loanDisbursermentRequest) {
        try {
            Integer loanId = loanDisbursermentRequest.getLoanId();
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("no loan with id " + loanId + " found"));

            Transaction transaction = new Transaction();

            if (loan.getLoanStatus() == LoanStatus.pending_disbursement) {
                float loanInterest = LoanCalculations.loanInterest(loanDisbursermentRequest.getAmount(),
                        loan.getProduct().getInterestRate());
                float totalAmount = LoanCalculations.totalAmount(loanDisbursermentRequest.getAmount(), loanInterest);

                if (loan.getInstallmentType().equals(InstallmentType.daily)) {
                    loan.setInstallmentAmount(totalAmount / 30);
                    loan.setNextInstallmentDate(LocalDate.now().plusDays(1));
                } else if (loan.getInstallmentType().equals(InstallmentType.weekly)) {
                    loan.setInstallmentAmount(totalAmount / 4);
                    loan.setNextInstallmentDate(LocalDate.now().plusDays(7));
                } else if(loan.getInstallmentType().equals(InstallmentType.bullet)){
                    loan.setInstallmentAmount(totalAmount);
                    loan.setNextInstallmentDate(LocalDate.now().plusDays(loan.getProduct().getDuration()));
                }

                loan.setLoanStatus(LoanStatus.active);
                loan.setLoanCurrentStatus(LoanStatus.active);
                loan.setDisbursementDate(LocalDate.now());
                loan.setDueDate(LocalDate.now().plusDays(loan.getProduct().getDuration()));
                loan.setPrincipal(loanDisbursermentRequest.getAmount());
                loan.setProcessingFee(LoanCalculations.processingFee(loanDisbursermentRequest.getAmount()));
                loan.setInterest(loanInterest);
                loan.setRemainingBalance(totalAmount);
                loan.setTotalAmount(totalAmount);

                transaction.setLoan(loan);
                transaction.setCustomer(loan.getCustomer());
                transaction.setAmount(loan.getTotalAmount());
                transaction.setType(TransactionType.disbursement);
                transaction.setStatus(TransactionStatus.completed);
                transaction.setTransactionCode(loanDisbursermentRequest.getTransactionCode().toUpperCase());
                transaction.setUser(officerRepository.findUserById(loanDisbursermentRequest.getUserId()));

                loanRepository.save(loan);
                transactionRepository.save(transaction);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public GenericResponse<List<PendingLoansDto>> getLoansWithApprovalStatus(String search, ApprovalStatus approvalStatus, int userId, Pageable pageable){
        List<PendingLoansDto> pendingLoansDtoList = new ArrayList<>();

        try{
            String role = getUserRole(userId);
            Integer branchId = BranchContext.get();
            log.info("Fetching loans with approval status: {} and role {}", approvalStatus, role);
            Page<Tuple> loans = loanRepository.getLoansWithApprovalStatus(search, approvalStatus, userId, role, branchId, pageable);

            for(Tuple loan : loans){
                PendingLoansDto dto = PendingLoansDto.builder()
                        .loanId(loan.get(0, Integer.class))
                        .customerName(loan.get(1, String.class))
                        .appliedAmount(loan.get(2, float.class))
                        .monthlyIncome(loan.get(3, float.class))
                        .phoneNumber(loan.get(4, String.class))
                        .purpose(loan.get(5, String.class))
                        .status(loan.get(6, ApprovalStatus.class))
                        .product(loan.get(7, String.class))
                        .officer(loan.get(8, String.class))
                        .applicationDate(loan.get(9, LocalDateTime.class))
                        .rejectionReason(loan.get(10, String.class) != null ? loan.get(10, String.class) : null)
                        .rejectionDate(loan.get(11, LocalDate.class) != null ? loan.get(11, LocalDate.class) : null)
                        .build();
                pendingLoansDtoList.add(dto);

            }
            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(loans.getNumber())
                    .totalElements(loans.getTotalElements())
                    .totalPages(loans.getTotalPages())
                    .limit(loans.getSize())
                    .build();

            return GenericResponse.<List<PendingLoansDto>>builder()
                    .data(pendingLoansDtoList)
                    .message("loans fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();
        }catch(Exception e){
            log.error("Error: {} ", e.getMessage());
            return GenericResponse.<List<PendingLoansDto>>builder()
                    .message("Error retrieving loans: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    private String getUserRole(Integer userId){
        return officerRepository.findRoleById(userId);
    }

    private <T> T getOrDefault(Tuple tuple, int index, Class<T> type, T defaultValue) {
        try {
            T value = tuple.get(index, type);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private List<LoansDto> mapLoansToDto(Page<Tuple> loans) {

        List<LoansDto> loansDtoList = new ArrayList<>();

        for (Tuple loan : loans) {

            LoanStatus loanStatus = loan.get(14, LoanStatus.class);
            String status = loanStatus != null ? loanStatus.name() : null;

            LoansDto dto = LoansDto.builder()
                    .loanId(getOrDefault(loan, 0, Integer.class, 0))
                    .customerName(getOrDefault(loan, 1, String.class, ""))
                    .principal(getOrDefault(loan, 2, Float.class, 0f))
                    .processingFee(getOrDefault(loan, 3, Float.class, 0f))
                    .interest(getOrDefault(loan, 4, Float.class, 0f))
                    .totalAmount(getOrDefault(loan, 5, Float.class, 0f))
                    .paidAmount(getOrDefault(loan, 6, Float.class, 0f))
                    .balance(getOrDefault(loan, 7, Float.class, 0f))
                    .monthlyIncome(getOrDefault(loan, 8, Float.class, 0f))
                    .installmentAmount(getOrDefault(loan, 9, Float.class, 0f))
                    .arrears(getOrDefault(loan, 10, Float.class, 0f))
                    .installmentType(getOrDefault(loan, 11, InstallmentType.class, null))
                    .phoneNumber(getOrDefault(loan, 12, String.class, ""))
                    .purpose(getOrDefault(loan, 13, String.class, ""))
                    .loanStatus(status)
                    .product(getOrDefault(loan, 15, String.class, ""))
                    .officer(getOrDefault(loan, 16, String.class, ""))
                    .dueDate(getOrDefault(loan, 17, LocalDate.class, null))
                    .daysRemaining(getOrDefault(loan, 18, Integer.class, 0))
                    .disburseDate(getOrDefault(loan, 19, LocalDate.class, null))
                    .defaultDate(getOrDefault(loan, 20, LocalDate.class, null))
                    .applicationDate(getOrDefault(loan, 21, LocalDate.class, null))
                    .appliedAmount(getOrDefault(loan, 22, Float.class, 0f))
                    .approvalDate(getOrDefault(loan, 23, LocalDate.class, null))
                    .customerId(getOrDefault(loan, 24, Integer.class, 0))
                    .build();

            loansDtoList.add(dto);
        }

        return loansDtoList;
    }



    @Override
    public GenericResponse<List<LoansDto>> getLoansWithStatus(String search, List<LoanStatus> status, int userId, Pageable pageable) {
        List<LoansDto> loansDtoList = new ArrayList<>();
        Integer branchId = BranchContext.get();

        try{
            String role =  getUserRole(userId);
            log.info("Fetching loans with status {}, branchId {}, role {} and userId {}", status, branchId, role, userId);

            Page<Tuple> loans = loanRepository.getLoans(search, status, userId, role, branchId, pageable);

            for(Tuple loan : loans){
                LoanStatus loanStatus = loan.get(14, LoanStatus.class);
                //LoanStatus loanCurrentStatus = loan.get(21, LoanStatus.class);

                LoansDto dto = LoansDto.builder()
                        .loanId(loan.get(0, Integer.class))
                        .customerName(loan.get(1, String.class))
                        .principal(loan.get(2, Float.class))
                        .processingFee(loan.get(3, Float.class))
                        .interest(loan.get(4, Float.class))
                        .totalAmount(loan.get(5, Float.class))
                        .paidAmount(loan.get(6, Float.class))
                        .balance(loan.get(7, Float.class))
                        .monthlyIncome(loan.get(8, Float.class))
                        .installmentAmount(loan.get(9, Float.class))
                        .arrears(loan.get(10, Float.class))
                        .installmentType(loan.get(11, InstallmentType.class))
                        .phoneNumber(loan.get(12, String.class))
                        .purpose(loan.get(13, String.class))
                        .loanStatus(loanHelpers.getLoanStatusDescription(loanStatus))
                        .product(loan.get(15, String.class))
                        .officer(loan.get(16, String.class))
                        .dueDate(loan.get(17, LocalDate.class))
                        .daysRemaining(loan.get(18, Integer.class))
                        .disburseDate(loan.get(19, LocalDate.class))
                        .defaultDate(loan.get(20, LocalDate.class))
                        .applicationDate(loan.get(21, LocalDate.class))
                        .appliedAmount(loan.get(22, Float.class))
                        .approvalDate(loan.get(23, LocalDate.class))
                        .customerId(loan.get(24, Integer.class))
                        //.loanCurrentStatus(getLoanStatusDescription(loanCurrentStatus))
                        .build();
                loansDtoList.add(dto);
            }
            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(loans.getNumber())
                    .totalElements(loans.getTotalElements())
                    .totalPages(loans.getTotalPages())
                    .limit(loans.getSize())
                    .build();

            return GenericResponse.<List<LoansDto>>builder()
                    .data(loansDtoList)
                    .message(" loans fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching loans {} ", e.getMessage());
            return GenericResponse.<List<LoansDto>>builder()
                    .message("Error retrieving  loans: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }

    }

    public List<LoansDto> getAllLoans(
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
            Pageable pageable
    ){
        try {
            Page<Tuple> loans = loanRepository.getAllLoans(search, status, userId, branchId, dueDay, dueFrom, dueTo, appliedDate, disbursedDate, customerId, pageable);
            return mapLoansToDto(loans);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error fetching loans: " +e.getMessage());
        }

    }

    @Override
    public void clearLoan(int id) {
        try {
            Loan loan = loanRepository.findById(id).orElseThrow(() -> new RuntimeException("No loan found with id " + id));
            loan.setLoanStatus(LoanStatus.paid);
            loan.setArrears(0F);
            loan.setRemainingBalance(0F);

            LoanClearedManually clearedManually = new LoanClearedManually();
            clearedManually.setInitialBalance(loan.getRemainingBalance());
            clearedManually.setInitialArrear(loan.getArrears());
            clearedManually.setReferenceId(loan.getId());

            clearedManuallyRepository.save(clearedManually);
            loanRepository.save(loan);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<DueLoansDto>> getDueLoansByRange(LocalDate fromDate, LocalDate toDate, int userId,  String search, Pageable pageable) {
        List<DueLoansDto> dueLoansDtoList = new ArrayList<>();
        Integer branchId = BranchContext.get();
        try{
            String role =   getUserRole(userId);
            log.info("Fetching loans due between {} and {} for userId {} and role {}", fromDate, toDate,  userId, role);
            Page<Tuple> dueLoans = loanRepository.getDueLoansByRange(fromDate, toDate, userId, search, List.of(LoanStatus.active, LoanStatus.partially_paid), role, branchId, pageable);
            return getListGenericResponse(dueLoansDtoList, dueLoans);
        } catch (Exception e) {
            log.error("Error fetching loans {} ", e.getMessage());
            return GenericResponse.<List<DueLoansDto>>builder()
                    .message("Error retrieving due loans: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    @Override
    public GenericResponse<DashboardSummaryDto> getLoansSummary(int userId) {
        try{
            String role = getUserRole(userId);
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

            DashboardSummaryDto dto = DashboardSummaryDto.builder()
                    .dueToday(loanRepository.duesCount(userId, LocalDate.now(), List.of(LoanStatus.active, LoanStatus.partially_paid), branchId, role))
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
                    .totalMonthlyCollection(repaymentRepository.totalMonthlyCollection(userId, LocalDateTime.now().getMonthValue(), role,branchId, RepaymentStatus.paid))
                    .interestEarnedToday(loanRepository.interestEarnedToday(userId, LoanStatus.active, role, branchId, LocalDate.now()))
                    .customersCount(customerRepository.countActiveCustomers(true, userId, role,branchId))
                    .returnCustomers(customerRepository.countReturnCustomers(role, userId, branchId))
                    .newCustomers(customerRepository.countOneTimeCustomers(role, userId, branchId))
                    .customersIncrease(customerIncrease)
                    .build();

            return GenericResponse.<DashboardSummaryDto>builder()
                    .data(dto)
                    .message("Summary retrieved successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .build();
        } catch (Exception e) {
            return GenericResponse.<DashboardSummaryDto>builder()
                    .message("Error retrieving summary: " + e.getMessage() +" ;"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    @Override
    public GenericResponse<List<RolledOverLoansDto>> getRolledOverLoans(Integer userId, String search, Pageable pageable) {
        List<RolledOverLoansDto> rolledOverLoansDtoList = new ArrayList<>();
        try {
            String role = getUserRole(userId);
            LoanStatus status = LoanStatus.rolled_over;
            Page<Tuple> rolledLoans = rolledOverLoansRepository.getRolledOverLoans(pageable, status, search, userId, role);

            for (Tuple loan : rolledLoans) {
                RolledOverLoansDto dto = RolledOverLoansDto.builder()
                        .preLoanId(loan.get(0, Integer.class))
                        .currLoanId(loan.get(1, Integer.class))
                        .balanceAtRollover(loan.get(2, Float.class))
                        .currBalance(loan.get(3, Float.class))
                        .prevPrincipal(loan.get(4, Float.class))
                        .currPrincipal(loan.get(5, Float.class))
                        .prevInterest(loan.get(6, Float.class))
                        .currInterest(loan.get(7, Float.class))
                        .prevTotalAmount(loan.get(8, Float.class))
                        .currTotalAmount(loan.get(9, Float.class))
                        .prevDue(loan.get(10, LocalDate.class))
                        .currDue(loan.get(11, LocalDate.class))
                        .rollOverDate(loan.get(12, LocalDate.class))
                        .customerName(loan.get(13, String.class))
                        .build();
                rolledOverLoansDtoList.add(dto);

            }

            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(rolledLoans.getNumber())
                    .totalElements(rolledLoans.getTotalElements())
                    .totalPages(rolledLoans.getTotalPages())
                    .limit(rolledLoans.getSize())
                    .build();

            return GenericResponse.<List<RolledOverLoansDto>>builder()
                    .data(rolledOverLoansDtoList)
                    .message("rolled-over loans fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();

        } catch (Exception e) {
            return GenericResponse.<List<RolledOverLoansDto>>builder()
                    .message("Error retrieving rolled-over loans: " + e.getMessage() +" ;"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }

    }

    @Override
    public GenericResponse<List<DueLoansDto>> getDueLoansByDay(  LocalDate day, int userId, String search, Pageable pageable) {
        List<DueLoansDto> dueLoansDtoList = new ArrayList<>();
        String role = getUserRole(userId);
        Integer branchId = BranchContext.get();
        log.info("Getting loans due on {} for userId {} and role {}",  day, userId, role);
        try{
            Page<Tuple> dueLoans = loanRepository.getDueLoansByDay(day, userId, search, role, List.of(LoanStatus.active, LoanStatus.partially_paid), branchId, pageable);
            return getListGenericResponse(dueLoansDtoList, dueLoans);
        } catch (Exception e) {
            log.error("Error fetching loans: {}",e.getMessage());
            return GenericResponse.<List<DueLoansDto>>builder()
                    .message("Error retrieving due loans: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    private GenericResponse<List<DueLoansDto>> getListGenericResponse(List<DueLoansDto> dueLoansDtoList, Page<Tuple> dueLoans) {
        for (Tuple loan : dueLoans) {
            DueLoansDto dto = DueLoansDto.builder()
                    .loanId(loan.get(0, Integer.class))
                    .customerName(loan.get(1, String.class))
                    .phoneNumber(loan.get(2, String.class))
                    .productName(loan.get(3, String.class))
                    .principal(loan.get(4, float.class))
                    .interest(loan.get(5, float.class))
                    .totalAmount(loan.get(6, float.class))
                    .paidAmount(loan.get(7, float.class))
                    .balance(loan.get(8, float.class))
                    .nextInstallmentDate(loan.get(9, LocalDate.class))
                    .dueDate(loan.get(10, LocalDate.class))
                    .loanStatus(loanHelpers.getLoanStatusDescription(loan.get(11, LoanStatus.class)))
                    .loanCurrentStatus(loanHelpers.getLoanStatusDescription(loan.get(12, LoanStatus.class)))
                    .daysRemaining(loan.get(13, Integer.class))
                    .build();
            dueLoansDtoList.add(dto);
        }
        ResponseMetaData meta = ResponseMetaData.builder()
                .page(dueLoans.getNumber())
                .totalElements(dueLoans.getTotalElements())
                .totalPages(dueLoans.getTotalPages())
                .limit(dueLoans.getSize())
                .build();

        return GenericResponse.<List<DueLoansDto>>builder()
                .data(dueLoansDtoList)
                .message("due loans fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .metaData(meta)
                .build();
    }

    @Override
    public void rollOverLoan(int loanId, float principal) {
        try{
            log.info("Rolling over loan with id {} and principal {} ", loanId,  principal);
            Loan loan = loanRepository.getRollOverLoan(loanId, List.of(LoanStatus.active, LoanStatus.defaulted, LoanStatus.partially_paid));
            if(loan == null)
                throw new ResourceNotFoundException("Loan not eligible for rollover");

            Loan newLoan = new Loan();

            float loanInterest = LoanCalculations.loanInterest(principal, loan.getProduct().getInterestRate());
            float totalAmount = LoanCalculations.totalAmount(principal, loanInterest);

            newLoan.setCustomer(loan.getCustomer());
            newLoan.setUser(loan.getUser());
            newLoan.setBranch(loan.getBranch());
            newLoan.setProduct(loan.getProduct());
            newLoan.setPrincipal(principal);
            newLoan.setAppliedAmount(principal);
            newLoan.setInterest(loanInterest);
            newLoan.setTotalAmount(totalAmount);
            newLoan.setInstallmentType(loan.getInstallmentType());
            //newLoan.setInstallmentAmount(loan.getInstallmentAmount());

            if (Objects.equals(loan.getInstallmentType(), InstallmentType.daily)) {
                newLoan.setInstallmentAmount(totalAmount / 30);
                newLoan.setNextInstallmentDate(LocalDate.now().plusDays(1));
            } else if (Objects.equals(loan.getInstallmentType(), InstallmentType.weekly)) {
                newLoan.setInstallmentAmount(totalAmount / 4);
                newLoan.setNextInstallmentDate(LocalDate.now().plusDays(7));
            }
            newLoan.setArrears(loan.getArrears());
            newLoan.setDueDate(LocalDate.now().plusDays(loan.getProduct().getDuration()));
            newLoan.setRemainingBalance(totalAmount);
            newLoan.setDisbursementDate(LocalDate.now());
            newLoan.setLoanStatus(LoanStatus.active);
            newLoan.setLoanCurrentStatus(LoanStatus.active);
            newLoan.setRolledOver(true);
            newLoan.setApplicationDate(LocalDate.now());

            loan.setLoanStatus(LoanStatus.rolled_over);

            RolledOverLoans rolledOverLoans = new RolledOverLoans();
            rolledOverLoans.setPrevLoan(loan);
            rolledOverLoans.setBranch(loan.getBranch());
            rolledOverLoans.setNewLoan(newLoan);
            rolledOverLoans.setDate(LocalDate.now());

            loanRepository.save(newLoan);
            loanRepository.save(loan);
            rolledOverLoansRepository.save(rolledOverLoans);

        } catch (Exception e) {
            log.error("Error rolling over loan: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rejectLoan(LoanRejectionRequest loanRejectionRequest) {
        try{
            log.info("Rejecting loan with id {}", loanRejectionRequest.getLoanId());
            Loan loan = loanRepository.findById(loanRejectionRequest.getLoanId())
                    .orElseThrow(() -> new ResourceNotFoundException("No loan with id " + loanRejectionRequest.getLoanId() + " found"));
            loan.setApprovalStatus(rejected);
            loan.setRejectionReason(loanRejectionRequest.getReason());
            loan.setRejectionDate(LocalDate.now());
            loanRepository.save(loan);
        } catch (Exception e) {
            log.error("Error rejecting loan: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<LoanDetailsDto> getLoanWithId(int id) {
        try{

            Tuple loan = loanRepository.findLoanDetailsById(id);

            List<PaymentDto> paymentDtoList = loanRepayments.getLoanRepayments(id);

            LoanDetailsDto details = LoanDetailsDto.builder()
                    .loanId(loan.get(0, Integer.class))
                    .customerName(loan.get(1, String.class))
                    .principal(loan.get(2, Float.class))
                    .processingFee(loan.get(3, Float.class))
                    .interest(loan.get(4, Float.class))
                    .totalAmount(loan.get(5, Float.class))
                    .paidAmount(loan.get(6, Float.class))
                    .balance(loan.get(7, Float.class))
                    .monthlyIncome(loan.get(8, Float.class))
                    .installmentAmount(loan.get(9, Float.class))
                    .arrears(loan.get(10, Float.class))
                    .installmentType(loan.get(11, InstallmentType.class))
                    .phoneNumber(loan.get(12, String.class))
                    .purpose(loan.get(13, String.class))
                    .loanStatus(loanHelpers.getLoanStatusDescription(loan.get(14, LoanStatus.class)))
                    .loanCurrentStatus(loanHelpers.getLoanStatusDescription(loan.get(15, LoanStatus.class)))
                    .product(loan.get(16, String.class))
                    .officer(loan.get(17, String.class))
                    .dueDate(loan.get(18, LocalDate.class))
                    .daysRemaining(loan.get(19, Integer.class))
                    .defaultDate(loan.get(20, LocalDate.class))
                    .applicationDate(loan.get(21, LocalDate.class))
                    .appliedAmount(loan.get(22, Float.class))
                    .approvalDate(loan.get(23, LocalDate.class))
                    .disburseDate(loan.get(24, LocalDate.class))
                    .updatedAt(loan.get(25, LocalDateTime.class))
                    .payments(!paymentDtoList.isEmpty() ? paymentDtoList : null)
                    .build();

            return GenericResponse.<LoanDetailsDto>builder()
                    .data(details)
                    .message("Loan fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching loan: {}", e.getMessage());
            return GenericResponse.<LoanDetailsDto>builder()
                    .message("Error retrieving loan: " + e.getMessage())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    public GenericResponse<List<MonthlyDataDto>> getMonthlyData(Long month, Long year, String type) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }


        List<MonthlyDataDto> monthlyDisbursementsDtoList = new ArrayList<>();

        try {
            List<Tuple> loans = null;

            if(type.equalsIgnoreCase("disbursement")) {
                loans = loanRepository.getOfficerMonthlyDisbursements(month, year);
            } else if (type.equalsIgnoreCase("collection")) {
                loans = repaymentRepository.getMonthlyCollections(month, year);
            }

            assert loans != null;
            for(Tuple loan : loans){
                Double target = 0.00;
                if(type.equalsIgnoreCase("disbursement")) {
                    target = loan.get(5, Double.class);
                } else if (type.equalsIgnoreCase("collection")) {
                    target = loan.get(6, Double.class);
                }

                Double amount = loan.get(4, Double.class);

                MonthlyDataDto dto = MonthlyDataDto.builder()
                        .officerId(loan.get(0, Integer.class))
                        .officerName(loan.get(1, String.class) + " " + loan.get(2, String.class))
                        .numberOfLoans(loan.get(3, Long.class))
                        .targetAmount(target)
                        .totalAmount(amount)
                        .deficit(target - amount)
                        .percentage((amount/target) * 100)
                        .build();
                monthlyDisbursementsDtoList.add(dto);
            }

            return GenericResponse.<List<MonthlyDataDto>>builder()
                    .data(monthlyDisbursementsDtoList)
                    .status(ResponseStatusEnum.SUCCESS)
                    .message("Data fetched successfully")
                    .build();

        } catch (Exception e) {
            return GenericResponse.<List<MonthlyDataDto>>builder()
                    .data(monthlyDisbursementsDtoList)
                    .status(ResponseStatusEnum.ERROR)
                    .message("Error fetching data: " + e.getMessage())
                    .build();
        }
    }

    // Add a separate method to get summary
//    @Override
//    public MonthlyDataSummaryDto getMonthlyDataSummary(Long month, Long year, String type) {
//        List<MonthlyDataDto> data = getMonthlyData(month, year, type);
//
//        long totalLoans = data.stream()
//                .mapToLong(MonthlyDataDto::getNumberOfLoans)
//                .sum();
//        double totalAmount = data.stream()
//                .mapToDouble(MonthlyDataDto::getTotalAmount)
//                .sum();
//        double totalMonthlyTarget = data.stream()
//                .mapToDouble(MonthlyDataDto::getTargetAmount)
//                .sum();
//
//        MonthlyDataSummaryDto summaryDto = new MonthlyDataSummaryDto();
//        summaryDto.setNumberOfLoans(totalLoans);
//        summaryDto.setTotalAmount(totalAmount);
//        summaryDto.setTargetAmount(totalMonthlyTarget);
//        summaryDto.setPercentage((totalAmount/totalMonthlyTarget) * 100);
//        summaryDto.setDeficit(totalMonthlyTarget - totalAmount);
//
//        return summaryDto;
//    }

    public List<MonthlyTrendResponse> getRolling12MonthTrends(Integer userId) {
        String role = getUserRole(userId);
        Integer branchId = BranchContext.get();

        List<MonthlyTrendProjection> disbursementData =
                loanRepository.findRolling12MonthDisbursementTrends(role, userId, branchId);

        List<MonthlyTrendProjection> collectionData =
                repaymentRepository.findRolling12MonthCollectionTrends(
                        role, userId, RepaymentStatus.paid.name(), branchId);

        // Key format: YYYY-MM
        Map<String, BigDecimal> disbursementMap = disbursementData.stream()
                .collect(Collectors.toMap(
                        p -> p.getYear() + "-" + p.getMonth(),
                        MonthlyTrendProjection::getSum
                ));

        Map<String, BigDecimal> collectionMap = collectionData.stream()
                .collect(Collectors.toMap(
                        p -> p.getYear() + "-" + p.getMonth(),
                        MonthlyTrendProjection::getSum
                ));

        List<MonthlyTrendResponse> trends = new ArrayList<>();

        LocalDate now = LocalDate.now().withDayOfMonth(1);

        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            int year = monthDate.getYear();
            int month = monthDate.getMonthValue();

            String key = year + "-" + month;

            BigDecimal disbursements = disbursementMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal collections = collectionMap.getOrDefault(key, BigDecimal.ZERO);

            // Only include months with value > 0
            if (
                    disbursements.compareTo(BigDecimal.ZERO) > 0 ||
                            collections.compareTo(BigDecimal.ZERO) > 0
            ) {
                trends.add(MonthlyTrendResponse.builder()
                        .year(year)
                        .month(month)
                        .monthName(monthDate.getMonth()
                                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .disbursements(disbursements)
                        .collections(collections)
                        .build());
            }

        }

        return trends;
    }

    public List<PaymentReportDto> getAllCollections(Integer officerId, LocalDate startDate, LocalDate endDate){
        log.info("getting collections for officerId: {}, startDate: {}, endDate: {}", officerId, startDate, endDate );
        List<PaymentReportDto> paymentReportDtoList = new ArrayList<>();
        try {
            User user = userDetailsService.getUserById(officerId);
            String role = String.valueOf(user.getRole());

            List<Tuple> payments = repaymentRepository.getCollectionsByOfficerIdAndRange(officerId, startDate.atStartOfDay(), role,  endDate.atStartOfDay());
            for(Tuple payment : payments){
                PaymentReportDto dto = PaymentReportDto.builder()
                        .id(payment.get(0, Integer.class))
                        .customerName(payment.get(1, String.class))
                        .amount(payment.get(2, Float.class))
                        .paymentDate(payment.get(3, LocalDateTime.class))
                        .officerName(payment.get(4, String.class))
                        .phoneNumber(payment.get(5, String.class))
                        .transactionCode(payment.get(6, String.class))
                        .build();
                paymentReportDtoList.add(dto);
            }
            return paymentReportDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DisbursementReportDto> getAllDisbursements(Integer officerId, LocalDate startDate, LocalDate endDate) {
        log.info("getting disbursements for officerId: {}, startDate: {}, endDate: {}", officerId, startDate, endDate );
        List<DisbursementReportDto> disbursementReportDtoList = new ArrayList<>();
        try {
            User user = userDetailsService.getUserById(officerId);
            String role = String.valueOf(user.getRole());

            List<Tuple> payments = loanRepository.getDisbursementsByOfficerIdAndRange(officerId, startDate, role, endDate);
            for (Tuple payment : payments) {
                DisbursementReportDto dto = DisbursementReportDto.builder()
                        .id(payment.get(0, Integer.class))
                        .customerName(payment.get(1, String.class))
                        .principal(payment.get(2, Float.class))
                        .totalAmount(payment.get(3, Float.class))
                        .interest(payment.get(4, Float.class))
                        .processingFee(payment.get(5, Float.class))
                        .date(payment.get(6, LocalDate.class))
                        .transactionCode(payment.get(7, String.class))
                        .phoneNumber(payment.get(8, String.class))
                        .build();
                disbursementReportDtoList.add(dto);
            }

            return disbursementReportDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
