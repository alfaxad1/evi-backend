package com.example.loanApp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "loans_cleared_manually")
public class LoanClearedManually {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "initial_balance")
    private Float initialBalance;

    @Column(name = "initial_arrear")
    private Float initialArrear;

}
