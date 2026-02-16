package com.example.loanApp.repository;

import com.example.loanApp.entities.Transaction;
import com.example.loanApp.enums.TransactionType;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("select t from Transaction t where t.repayment.id = :repaymentId")
    Transaction findTransaction(int repaymentId);

    @Query("select t.id, concat(c.firstName, ' ', c.lastName), t.transactionCode, " +
            "t.amount, t.createdAt, t.status " +
            "from Transaction t " +
            "left join t.user u " +
            "join t.customer c " +
            "where t.type = :type " +
            "AND (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or u.branch.id = :branchId)")
    Page<Tuple> findTransactionsByStatus(TransactionType type, Integer userId, String role, Integer branchId, Pageable pageable);
}
