package com.example.loanApp.entities;

import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "expense_date")
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type")
    private ExpenseType expenseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Column(name = "item")
    private String item;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "no_of_items")
    private BigDecimal noOfItems;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "transaction_cost")
    private BigDecimal transactionCost;

    @Column(name = "recipient_name")
    private String recipientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Column(name = "receipt_url", length = 512)
    private String receiptUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by")
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
