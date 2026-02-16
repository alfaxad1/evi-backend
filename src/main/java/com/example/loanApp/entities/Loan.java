package com.example.loanApp.entities;

import com.example.loanApp.enums.ApprovalStatus;
import com.example.loanApp.enums.InstallmentType;
import com.example.loanApp.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "applied_amount")
    private Float appliedAmount;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "arrears")
    private Float arrears = 0.00F;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "default_date")
    private LocalDate defaultDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "installment_amount")
    private Float installmentAmount;

    @Column(name = "installment_type")
    @Enumerated(EnumType.STRING)
    private InstallmentType installmentType;

    @Column(name = "total_interest")
    private Float interest;

//    @Column(name = "next_due_date")
//    private LocalDate nextDueDate;

//    @Size(max = 255)
//    @Column(name = "phone")
//    private String phone;

    @Column(name = "principal", nullable = false)
    private Float principal;

    @Column(name = "processing_fee", nullable = false)
    private Float processingFee;

    @Size(max = 255)
    @Column(name = "purpose")
    private String purpose;

    @Column(name = "rejection_date")
    private LocalDate rejectionDate;

    @Size(max = 255)
    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "remaining_balance", nullable = false)
    private Float remainingBalance = 0.00F;

    @Column(name = "total_amount", nullable = false)
    private Float totalAmount;

//    @Size(max = 255)
//    @Column(name = "transaction_code")
//    private String transactionCode;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private LoanProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", referencedColumnName = "id")
    private User user;

    @Column(name = "installments_sum", nullable = false)
    private Float paidAmount = 0.00F;

    @Column(name = "completed_date")
    private LocalDate paidDate;

    @Column(name = "expected_completion_date")
    private LocalDate dueDate;

    @Column(name = "due_date")
    private LocalDate nextInstallmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_current_status")
    private LoanStatus loanCurrentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LoanStatus loanStatus;

    @Column(name = "rolled_over", nullable = false)
    private Boolean rolledOver = false;

}