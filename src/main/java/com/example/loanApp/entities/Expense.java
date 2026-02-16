package com.example.loanApp.entities;

import com.example.loanApp.enums.ExpenseType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type")
    private ExpenseType expenseType;

    @Column(name = "item")
    private String item;

    @Column(name = "unit_cost")
    private Float unitCost;

    @Column(name = "no_of_items")
    private BigDecimal noOfItems;

    @Column(name = "total_cost")
    private Float totalCost;

    @Column(name = "description")
    private String description;
}
