package com.example.loanApp.repository;

import com.example.loanApp.dtos.MonthlyTrendProjection;
import com.example.loanApp.dtos.WeeklyStatsProjection;
import com.example.loanApp.entities.Repayment;
import com.example.loanApp.enums.RepaymentStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Integer> {

    @Query("SELECT u.id, u.firstName, u.lastName, " +
            "COUNT(r.id), " +
            "COALESCE(SUM(r.amount), 0), " +
            "u.monthlyDisbursementTarget , u.monthlyCollectionTarget " +
            "FROM Repayment r " +
            "JOIN r.user u " +
            "WHERE MONTH(r.paymentDate) =:month " +
            "AND YEAR(r.paymentDate) =:year " +
            "GROUP BY u.firstName, u.lastName, u.id")
    List<Tuple> getMonthlyCollections(@Param("month") Long month, @Param("year")Long year);

    @Query("select r.id, concat(c.firstName, ' ', c.lastName), r.amount, r.paymentDate, u.firstName, " +
            "c.phone, r.transactionCode " +
            "from Repayment r " +
            "join r.loan.customer c " +
            "left join r.user u " +
            "where r.paymentDate between :startDate and :endDate " +
            "AND (:role = 'admin' OR u.id = :officerId)" +
            "and r.status = 'paid'")
    List<Tuple> getCollectionsByOfficerIdAndRange
            (Integer officerId, LocalDateTime startDate, String role, LocalDateTime endDate);

    @Query("select coalesce(sum(r.amount), 0) from Repayment r " +
            "left join r.user u " +
            "where r.status = :status " +
            "and function('date', r.paymentDate) = :day " +
            "AND (:role = 'admin' OR u.id = :userId)")
    float dailyTotalRepayments(int userId, LocalDate day, String role, RepaymentStatus status);

    @Query("select coalesce(sum(r.amount), 0) from Repayment r " +
            "left join r.user u " +
            "where r.status = :status " +
            "and month(r.paymentDate)= :month " +
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or r.branch.id = :branchId)")
    float totalMonthlyCollection(int userId, Integer month, String role, Integer branchId, RepaymentStatus status);

    @Query("select r.loan.id, concat(r.loan.customer.firstName, ' ', r.loan.customer.lastName), " +
            "r.paymentDate, r.status, r.transactionCode, r.paymentName, l.loanStatus, l.totalAmount, r.amount " +
            "from Repayment r " +
            "join r.loan l " +
            "where l.id = :loanId " +
            "and r.status = :status")
    List<Tuple> getLoanRepayments(int loanId, RepaymentStatus status);

    @Query("select r from Repayment r " +
            "where r.status = :repaymentStatus " +
            "AND (:search IS NULL " +
            "OR LOWER(r.paymentName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.transactionCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            ")")
    Page<Repayment> getPendingPayments(RepaymentStatus repaymentStatus, String search, Pageable pageable);

    @Query("select r.loan.id, concat(c.firstName, ' ', c.lastName), " +
            "r.paymentDate, r.status, r.transactionCode, r.paymentName, l.loanStatus, l.totalAmount, r.amount " +
            "from Repayment r " +
            "join r.loan l " +
            "join l.customer c " +
            "left join r.user u " +
            "where (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or r.branch.id = :branchId) " +
            "and r.status = :repaymentStatus " +
            "AND (:search IS NULL " +
            "OR LOWER(r.paymentName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.transactionCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            ")")
    Page<Tuple> getApprovedPayments(int userId, RepaymentStatus repaymentStatus, String role, String search, Integer branchId, Pageable pageable);

    @Query("select r from Repayment r where r.id = :repaymentId and r.status = :repaymentStatus")
    Repayment findRepayment(int repaymentId, RepaymentStatus repaymentStatus);

    @Query(value = """
    SELECT
        EXTRACT(YEAR FROM r.paid_date) AS year,
        EXTRACT(MONTH FROM r.paid_date) AS month,
        SUM(r.amount) AS sum
    FROM repayments r
    LEFT JOIN loans l ON r.loan_id = l.id
    LEFT JOIN users u ON l.officer_id = u.id
    WHERE
        r.status = :status
        AND r.paid_date >= CURRENT_DATE - INTERVAL 12 MONTH
        AND (:role = 'admin' OR u.id = :userId)
        and (:branchId is null or r.branch_id = :branchId)
    GROUP BY
        EXTRACT(YEAR FROM r.paid_date),
        EXTRACT(MONTH FROM r.paid_date)
    HAVING SUM(r.amount) > 0
    ORDER BY
        year, month
""", nativeQuery = true)
    List<MonthlyTrendProjection> findRolling12MonthCollectionTrends(
            String role,
            Integer userId,
            String status,
            Integer branchId
    );



    @Query(value = "SELECT " +
            "  DATE_FORMAT(r.paid_date, '%a') as day, " +
            "  SUM(r.amount) as amount, " +
            "  COUNT(*) as count " +
            "FROM repayments r " +
            "LEFT JOIN users u ON r.created_by = u.id " +
            "WHERE YEARWEEK(r.paid_date, 1) = YEARWEEK(:targetDate, 1) " +
            "AND (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or r.branch_id = :branchId) " +
            "GROUP BY day, DAYOFWEEK(r.paid_date) " +
            "ORDER BY DAYOFWEEK(r.paid_date)",
            nativeQuery = true)
    List<WeeklyStatsProjection> getStatsBySpecificWeek(
            @Param("targetDate") String targetDate,
            @Param("userId") Integer userId,
            @Param("role") String role,
            Integer branchId
    );
}
