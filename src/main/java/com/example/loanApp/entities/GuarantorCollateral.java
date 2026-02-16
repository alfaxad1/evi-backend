package com.example.loanApp.entities;

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
@Table(name = "guarantor_collaterals")
@EntityListeners(AuditingEntityListener.class)
public class GuarantorCollateral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarantor_id", referencedColumnName = "id")
    private Guarantor guarantor;

    @Size(max = 255)
    @NotNull
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Lob
    @Column(name = "additional_details")
    private String additionalDetails;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}