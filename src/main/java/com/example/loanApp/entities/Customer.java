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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Size(max = 50)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Size(max = 50)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "national_id", length = 30)
    private String nationalId;

    @Size(max = 255)
    @Column(name = "national_id_photo")
    private String nationalIdPhoto;

    @Size(max = 255)
    @Column(name = "passport_photo")
    private String passportPhoto;

    //@NotNull
    @Column(name = "date_of_birth", length = 20)
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "gender")
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "residence_details")
    private String residenceDetails;

//    @Size(max = 50)
    @Column(name = "county", length = 50)
    private String county;

    @NotNull
    @Size(max = 100)
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Size(max = 100)
    @Column(name = "business_name", length = 100)
    private String businessName;

    @Column(name = "business_location")
    private String businessLocation;

    @NotNull
    @Column(name = "monthly_income")
    private Float monthlyIncome;

    @ColumnDefault("0")
    @Column(name = "credit_score")
    private Integer creditScore;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User user;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerCollateral> customerCollaterals = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Guarantor> guarantors = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Referee> referees = new ArrayList<>();

}