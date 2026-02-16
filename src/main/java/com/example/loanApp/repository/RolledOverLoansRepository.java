package com.example.loanApp.repository;

import com.example.loanApp.entities.RolledOverLoans;
import com.example.loanApp.enums.LoanStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RolledOverLoansRepository extends JpaRepository<RolledOverLoans, Integer> {

    @Query("select p.id, n.id, p.remainingBalance, n.remainingBalance, p.principal, n.principal, " +
            "p.interest, n.interest, p.totalAmount, n.totalAmount, p.dueDate, n.dueDate, ro.date, " +
            "concat(p.customer.firstName, '', p.customer.lastName)  " +
            "from RolledOverLoans ro " +
            "join ro.prevLoan p " +
            "join ro.newLoan n " +
            "where (:role = 'admin' OR p.user.id = :userId) " +
            "AND (:search IS NULL " +
            "OR LOWER(p.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tuple> getRolledOverLoans(Pageable pageable, LoanStatus status, String search, Integer userId, String role);
}
