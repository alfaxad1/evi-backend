package com.example.loanApp.entities;

import com.example.loanApp.enums.TransactionStatus;
import com.example.loanApp.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "mpesa_transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="repayment_id", referencedColumnName = "id")
    private Repayment repayment;

    @NotNull
    @Column(name = "amount", nullable = false)
    private float amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Size(max = 50)
    @NotNull
    @Column(name = "mpesa_code", nullable = false, length = 50)
    private String transactionCode;

    @ColumnDefault("'pending'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", referencedColumnName = "id")
    private User user;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}