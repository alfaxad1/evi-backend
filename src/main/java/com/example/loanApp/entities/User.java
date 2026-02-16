package com.example.loanApp.entities;

import com.example.loanApp.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;

    //    @Size(max = 50)
//    @NotNull
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

//    @Size(max = 50)
//    @NotNull
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

//    @Size(max = 100)
//    @NotNull
    @Column(name = "email")
    private String email;

    //@NotNull
    @Column(name = "phone_number")
    private String phoneNumber;

//    @Size(max = 255)
//    @NotNull
    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "disbursement_target")
    private Double monthlyDisbursementTarget;

    @Column(name = "collection_target")
    private Double monthlyCollectionTarget;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private boolean isActive =  true;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 255)
    @Column(name = "avatar")
    private String avatar;

}