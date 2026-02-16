package com.example.loanApp.entities;

import com.example.loanApp.enums.RepaymentStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "repayments")
@EntityListeners(AuditingEntityListener.class)
public class Repayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private Loan loan;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "due_date")
    private LocalDate dueDate;


    @Column(name = "paid_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RepaymentStatus status;

    @Size(max = 50)
    @Column(name = "mpesa_code", length = 50)
    private String transactionCode;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phone;

    @Size(max = 255)
    @Column(name = "payment_name")
    private String paymentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User user;

    @Column(name = "verified_by")
    private Integer verifiedBy;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}