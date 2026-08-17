package com.example.loanApp.repository;

import com.example.loanApp.entities.Expense;
import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @Query("""
            select e from Expense e
            where (:branchId is null or e.branch.id = :branchId)
              and (:category is null or e.category = :category)
              and (:status is null or e.status = :status)
              and (:expenseType is null or e.expenseType = :expenseType)
              and (:from is null or e.expenseDate >= :from)
              and (:to is null or e.expenseDate <= :to)
              and (:search is null or lower(e.item) like lower(concat('%', :search, '%'))
                   or lower(coalesce(e.description, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(e.recipientName, '')) like lower(concat('%', :search, '%')))
            order by e.expenseDate desc, e.id desc
            """)
    Page<Expense> search(@Param("branchId") Integer branchId,
                         @Param("category") ExpenseCategory category,
                         @Param("status") ExpenseStatus status,
                         @Param("expenseType") ExpenseType expenseType,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         @Param("search") String search,
                         Pageable pageable);

    @Query("""
            select e from Expense e
            where (:branchId is null or e.branch.id = :branchId)
              and e.expenseDate between :from and :to
            order by e.expenseDate desc, e.id desc
            """)
    List<Expense> findInPeriod(@Param("branchId") Integer branchId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);

    @Query("""
            select e from Expense e
            where (:branchId is null or e.branch.id = :branchId)
              and e.expenseType = com.example.loanApp.enums.ExpenseType.OFFICER_REIMBURSEMENT
              and e.status in (com.example.loanApp.enums.ExpenseStatus.PENDING,
                               com.example.loanApp.enums.ExpenseStatus.APPROVED)
            """)
    List<Expense> findPendingReimbursements(@Param("branchId") Integer branchId);
}
