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

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "referees")
@EntityListeners(AuditingEntityListener.class)
public class Referee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @Column(name = "id_number", nullable = false, length = 50)
    private String idNumber;

    @Size(max = 20)
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @NotNull
    @Column(name = "relationship", nullable = false, length = 100)
    private String relationship;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}