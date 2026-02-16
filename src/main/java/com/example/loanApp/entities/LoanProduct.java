package com.example.loanApp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "loan_products")
@EntityListeners(AuditingEntityListener.class)
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "min_amount", nullable = false)
    private Float minAmount;

    @NotNull
    @Column(name = "max_amount", nullable = false)
    private Float maxAmount;

    @NotNull
    @Column(name = "interest_rate", nullable = false)
    private float interestRate;

    @NotNull
    @Column(name = "duration_days", nullable = false)
    private Integer duration;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}