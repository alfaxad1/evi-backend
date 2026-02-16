package com.example.loanApp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.loanApp.Mappers.PaymentMapper;
import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.Customer;
import com.example.loanApp.entities.Loan;
import com.example.loanApp.entities.Repayment;
import com.example.loanApp.entities.Transaction;
import com.example.loanApp.enums.*;
import com.example.loanApp.repository.*;
import com.example.loanApp.utility.LoanHelpers;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loanApp.utility.LoanCalculations;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final TransactionRepository transactionRepository;
    private final LoanHelpers loanHelpers;
    private final CustomerRepository customerRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository officerRepository;

    @Transactional
    @Override
    public void repayment(RepaymentRequest request) {
        String narration = request.getNarration();

        try {
            String transactionCode = handleNarration(narration)[0];
            String paymentName = handleNarration(narration)[3];
            String phoneNumber = handleNarration(narration)[2];
            LocalDateTime paymentDate = request.getTransactionDate();

//            if(!phoneNumber.startsWith("254") || transactionCode.startsWith("MPESAC2B")){
//                saveAccountCredit();
//                return;
//            }

            Customer customer = customerRepository.findByPhone(phoneNumber);
            if(customer == null) {
                handlePhoneNotFoundPayment(request);
                return;
            }

            Integer customerId = customer.getId();
            Loan loan = loanRepository.findLatestByCustomerId(customerId);

            Repayment repayment = new Repayment();
            Transaction transaction = new Transaction();

            float amount = LoanCalculations.calculateRepaymentAmount(request.getAmount());
            float arrears = LoanCalculations.calculateArrears(amount, loan.getArrears(), loan.getInstallmentAmount());
            float balance = LoanCalculations.calculateBalance(loan.getRemainingBalance(), amount, loan.getTotalAmount());
            float paidAmount = loan.getTotalAmount() - balance;
            LocalDate nextDueDate = LoanCalculations.setNextDueDate(loan.getNextInstallmentDate(), loan.getInstallmentType());
            LoanStatus loanStatus = LoanCalculations.changeLoanCurrentStatus(
                    balance, loan.getDueDate(), paidAmount, loan.getTotalAmount()
            );
            LoanStatus currentLoanStatus = LoanCalculations.changeLoanStatus(
                    balance, loan.getDueDate(), paidAmount, loan.getTotalAmount()
            );

            loan.setArrears(arrears);
            loan.setNextInstallmentDate(nextDueDate);
            loan.setRemainingBalance(balance);
            loan.setPaidAmount(paidAmount);
            loan.setLoanStatus(loanStatus);
            loan.setLoanCurrentStatus(currentLoanStatus);

            repayment.setLoan(loan);
            repayment.setBranch(loan.getBranch());
            repayment.setAmount(amount);
            repayment.setDueDate(nextDueDate);
            repayment.setPaymentDate(paymentDate);
            repayment.setTransactionCode(transactionCode);
            repayment.setPhone(phoneNumber);
            repayment.setPaymentName(paymentName);
            repayment.setStatus(RepaymentStatus.paid);
            repayment.setUser(loan.getUser());

            transaction.setRepayment(repayment);
            transaction.setCustomer(loan.getCustomer());
            transaction.setLoan(loan);
            transaction.setBranch(loan.getBranch());
            transaction.setAmount(amount);
            transaction.setType(TransactionType.repayment);
            transaction.setTransactionCode(transactionCode);
            transaction.setStatus(TransactionStatus.completed);
            transaction.setUser(loan.getUser());

            transactionRepository.save(transaction);
            repaymentRepository.save(repayment);
            loanRepository.save(loan);
        } catch (Exception e) {
            log.error("Repayment request exception", e);
            throw new RuntimeException("Error creating payment: " +e.getMessage() +" : " +e.getCause());
        }
    }

//    private void saveAccountCredit(){
//
//    }

    private String[] handleNarration(String narration){
        if (narration == null) {
            throw new IllegalArgumentException("Narration cannot be null");
        }
        String[] narrationParts = narration.split("~");
        if (narrationParts.length < 4) {
            throw new IllegalArgumentException("Narration format is invalid");
        }

        return narrationParts;
    }

    private void handlePhoneNotFoundPayment(RepaymentRequest request){
        try{
            Repayment repayment = new Repayment();
            Transaction transaction = new Transaction();

            String narration = request.getNarration();
            String transactionCode = handleNarration(narration)[0];
            String paymentName = handleNarration(narration)[3];
            String phoneNumber = handleNarration(narration)[2];
            String dateStr = handleNarration(narration)[4];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime paymentDate = LocalDateTime.parse(dateStr, formatter);

            float amount = LoanCalculations.calculateRepaymentAmount(request.getAmount());

            repayment.setAmount(amount);
            repayment.setPaymentDate(paymentDate);
            repayment.setTransactionCode(transactionCode);
            repayment.setPhone(phoneNumber);
            repayment.setPaymentName(paymentName);
            repayment.setStatus(RepaymentStatus.pending);

            transaction.setRepayment(repayment);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.repayment);
            transaction.setTransactionCode(transactionCode);
            transaction.setStatus(TransactionStatus.completed);

            repaymentRepository.save(repayment);
            transactionRepository.save(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Error handling phoneNumberNotFoundPayment" +"\n" + "Message: "+e.getMessage() +"\n" +"Cause: "+e.getCause());
        }
    }

    private String getUserRole(Integer userId){
        return officerRepository.findRoleById(userId);
    }

    @Override
    public void resolvePayment(int loanId, int repaymentId) {

        try {
            Loan loan = loanRepository.findLoan(loanId, List.of(LoanStatus.active, LoanStatus.defaulted, LoanStatus.partially_paid));
            Repayment payment = repaymentRepository.findRepayment(repaymentId, RepaymentStatus.pending);
            Transaction transaction = transactionRepository.findTransaction(repaymentId);

            float arrears = LoanCalculations.calculateArrears(payment.getAmount(), loan.getArrears(), loan.getInstallmentAmount());
            LocalDate nextInstallmentDate = LoanCalculations.setNextDueDate(loan.getNextInstallmentDate(), loan.getInstallmentType());
            float balance = LoanCalculations.calculateBalance(loan.getRemainingBalance(), payment.getAmount(), loan.getTotalAmount());
            float paidAmount = loan.getTotalAmount() - balance;
            LoanStatus loanStatus = LoanCalculations.changeLoanStatus(
                    balance, loan.getDueDate(), paidAmount, loan.getTotalAmount()
            );
            LoanStatus currentLoanStatus = LoanCalculations.changeLoanStatus(
                    balance, loan.getDueDate(), paidAmount, loan.getTotalAmount()
            );

            loan.setArrears(arrears);
            loan.setNextInstallmentDate(nextInstallmentDate);
            loan.setRemainingBalance(balance);
            loan.setPaidAmount(paidAmount);
            loan.setLoanStatus(loanStatus);
            loan.setLoanCurrentStatus(currentLoanStatus);

            transaction.setCustomer(loan.getCustomer());
            transaction.setLoan(loan);
            transaction.setBranch(loan.getBranch());
            transaction.setUser(loan.getUser());

            payment.setLoan(loan);
            payment.setDueDate(nextInstallmentDate);
            payment.setStatus(RepaymentStatus.paid);
            payment.setBranch(loan.getBranch());
            payment.setUser(loan.getUser());

            loanRepository.save(loan);
            transactionRepository.save(transaction);
            repaymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Error resolving payment" +"\n" + "Message: "+e.getMessage() +"\n" +"Cause: "+e.getCause());
        }
    }

    @Override
    public GenericResponse<List<PaymentDto>> getPendingPayments(String search, Pageable pageable) {
        try {
            Page<Repayment> payments = repaymentRepository.getPendingPayments(RepaymentStatus.pending, search, pageable);
            Page<PaymentDto> dto = payments.map(paymentMapper::toDto);

            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(payments.getNumber())
                    .totalElements(payments.getTotalElements())
                    .totalPages(payments.getTotalPages())
                    .limit(payments.getSize())
                    .build();

            return GenericResponse.<List<PaymentDto>>builder()
                    .data(dto.getContent())
                    .message("pending payments fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();

        } catch (Exception e) {
            return GenericResponse.<List<PaymentDto>>builder()
                    .message("Error retrieving pending payments: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    @Override
    public GenericResponse<List<PaymentDto>> getApprovedPayments(int userId, String search, Pageable pageable) {
        List<PaymentDto> paymentDtoList = new ArrayList<>();
        try {
            String role =  getUserRole(userId);
            Integer branchId = BranchContext.get();

            Page<Tuple> payments = repaymentRepository.getApprovedPayments(userId, RepaymentStatus.paid, role, search, branchId, pageable);
            for (Tuple payment : payments) {
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

            ResponseMetaData meta = ResponseMetaData.builder()
                    .page(payments.getNumber())
                    .totalElements(payments.getTotalElements())
                    .totalPages(payments.getTotalPages())
                    .limit(payments.getSize())
                    .build();

            return GenericResponse.<List<PaymentDto>>builder()
                    .data(paymentDtoList)
                    .message("payments fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .metaData(meta)
                    .build();

        } catch (Exception e) {
            return GenericResponse.<List<PaymentDto>>builder()
                    .message("Error retrieving payments: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }
    }

    public WeeklyRepaymentResponse getWeeklyStats(String date, Integer userId){
        try {
            String role = getUserRole(userId);
            Integer branchId = BranchContext.get();
            String targetDate = date != null ? date : LocalDate.now().toString();
            List<WeeklyStatsProjection> stats = repaymentRepository.getStatsBySpecificWeek(targetDate, userId, role, branchId);
            Double total = stats.stream().mapToDouble(WeeklyStatsProjection::getAmount).sum();

            return new WeeklyRepaymentResponse(total, stats);
        }catch (Exception e){
            throw new RuntimeException("Error resolving weekly stats: " + e.getMessage());
        }
    }

    @Transactional
    public void manualRepayment(Float amount, LocalDateTime date, String transactionCode, Integer loanId) {
        try {

            Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan id not found"));

            String phoneNumber = loan.getCustomer().getPhone();
            String customerName = loan.getCustomer().getFirstName();
            String narration = buildNarration(transactionCode, phoneNumber, customerName);

            float reversedAmount = LoanCalculations.reverseRepaymentAmount(amount);

            RepaymentRequest request = new RepaymentRequest(reversedAmount, narration, date);

            repayment(request);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildNarration(String transactionCode,
                                  String phoneNumber,
                                 String customerName) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String currentDateTime =
                LocalDateTime.now().format(formatter);

        return String.join("~",
                transactionCode.toUpperCase(),
                "111111",
                phoneNumber,
                customerName,
                currentDateTime
        );
    }

}

