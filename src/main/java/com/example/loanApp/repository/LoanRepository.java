package com.example.loanApp.repository;

import com.example.loanApp.dtos.MonthlyTrendProjection;
import com.example.loanApp.entities.Customer;
import com.example.loanApp.entities.Loan;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.LoanStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loanApp.enums.ApprovalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    @Query("SELECT l.id, CONCAT(l.customer.firstName, ' ', l.customer.lastName), l.appliedAmount, l.customer.monthlyIncome, l.customer.phone, " +
            "l.purpose, l.approvalStatus, l.product.name, l.user.firstName, l.createdAt, l.rejectionReason, l.rejectionDate " +
            "FROM Loan l " +
            "LEFT JOIN l.user u " +
            "WHERE l.approvalStatus =:approvalStatus " +
            "AND (:search IS NULL " +
            "OR LOWER(l.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(l.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            ")" +
            "and (:branchId is null or l.branch.id = :branchId)" +
            "AND (:role = 'admin' OR u.id = :userId)")
    Page<Tuple> getLoansWithApprovalStatus(String search, ApprovalStatus approvalStatus, int userId, String role, Integer branchId, Pageable pageable);

    List<Loan> findByDueDateBeforeAndLoanStatusNot(LocalDate date, LoanStatus loanStatus);

    List<Loan> findByNextInstallmentDateBeforeAndLoanStatusAndLoanCurrentStatus(LocalDate date, LoanStatus loanStatus, LoanStatus loanCurrentStatus);

    @Query("SELECT u.id, u.firstName, u.lastName, COUNT(l.id), COALESCE(SUM(l.principal), 0), " +
            "u.monthlyDisbursementTarget, u.monthlyCollectionTarget " +
            "FROM Loan l " +
            "JOIN l.user u " +
            "WHERE MONTH(l.disbursementDate) =:month " +
            "AND YEAR(l.disbursementDate) =:year " +
            "GROUP BY u.firstName, u.lastName, u.id " +
            "ORDER BY u.id, u.firstName, u.lastName")
    List<Tuple> getOfficerMonthlyDisbursements(@Param("month") Long month, @Param("year")Long year);

    @Query("""
        SELECT l.id, CONCAT(c.firstName, ' ', c.lastName), l.principal, l.processingFee,
               l.interest, l.totalAmount, l.paidAmount, l.remainingBalance, c.monthlyIncome,
               l.installmentAmount, l.arrears, l.installmentType, c.phone, l.purpose,
               l.loanStatus, p.name, u.firstName, l.dueDate,
               FUNCTION('DATEDIFF', l.dueDate, current date),
               l.disbursementDate, l.defaultDate, l.applicationDate, l.appliedAmount,
               l.approvalDate, l.customer.id
        FROM Loan l
        JOIN l.customer c
        JOIN l.product p
        LEFT JOIN l.user u
        WHERE l.loanStatus in (:status)
        AND (:search IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
        and (:branchId is null or l.branch.id = :branchId)
        AND (:role = 'admin' OR u.id = :userId)
    """)
    Page<Tuple> getLoans(
            @Param("search") String search,
            @Param("status") List<LoanStatus> status,
            @Param("userId") int userId,
            @Param("role") String role,
            Integer branchId,
            Pageable pageable
    );

    @Query("""
    SELECT l.id, CONCAT(c.firstName, ' ', c.lastName), l.principal, l.processingFee,
           l.interest, l.totalAmount, l.paidAmount, l.remainingBalance, c.monthlyIncome,
           l.installmentAmount, l.arrears, l.installmentType, c.phone, l.purpose,
           l.loanStatus, p.name, u.firstName, l.dueDate,
           FUNCTION('DATEDIFF', l.dueDate, current date),
           l.disbursementDate, l.defaultDate, l.applicationDate, l.appliedAmount,
           l.approvalDate, l.customer.id
    FROM Loan l
    JOIN l.customer c
    JOIN l.product p
    LEFT JOIN l.user u
    WHERE
    (
        (:status IS NOT NULL AND l.loanStatus IN :status)
        OR
        (:status IS NULL AND l.loanStatus IN ('active', 'partially_paid', 'paid', 'rolled_over', 'defaulted'))
    )
    AND (:search IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
    AND (:branchId IS NULL OR l.branch.id = :branchId)
    AND (:userId IS NULL OR u.id = :userId)
    AND (:dueDay IS NULL OR l.dueDate = :dueDay)
    AND (:dueFrom IS NULL OR :dueTo IS NULL OR l.dueDate BETWEEN :dueFrom AND :dueTo)
    AND (:appliedDate IS NULL OR l.applicationDate = :appliedDate)
    AND (:disbursedDate IS NULL OR l.disbursementDate = :disbursedDate)
    AND (:customerId IS NULL OR c.id = :customerId)
    ORDER BY l.id DESC
""")

    Page<Tuple> getAllLoans(
            String search,
            List<LoanStatus> status,
            Integer userId,
            Integer branchId,
            LocalDate dueDay,
            LocalDate dueFrom,
            LocalDate dueTo,
            LocalDate appliedDate,
            LocalDate disbursedDate,
            Integer customerId,
            Pageable pageable
    );

    @Query("SELECT l.id, CONCAT(c.firstName, ' ', c.lastName), c.phone, p.name, " +
            "l.principal, l.interest, l.totalAmount, l.paidAmount, l.remainingBalance, " +
            "l.nextInstallmentDate, l.dueDate, l.loanStatus, l.loanCurrentStatus, " +
            "FUNCTION('DATEDIFF', l.dueDate, current date) " +
            "FROM Loan l " +
            "JOIN l.customer c " +
            "JOIN l.product p " +
            "LEFT JOIN l.user u " +
            "WHERE l.dueDate = :day " +
            "and l.loanStatus in (:status)" +
            "AND (:search IS NULL " +
            "OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            ")" +
            "and (:branchId is null or l.branch.id = :branchId)" +
            "AND (:role = 'admin' OR u.id = :userId)")
    Page<Tuple> getDueLoansByDay(LocalDate day, int userId,  String search, String role,List<LoanStatus> status, Integer branchId, Pageable pageable);

    @Query("SELECT l.id, CONCAT(c.firstName, ' ', c.lastName), c.phone, p.name, " +
            "l.principal, l.interest, l.totalAmount, l.paidAmount, l.remainingBalance, " +
            "l.nextInstallmentDate, l.dueDate, l.loanStatus, l.loanCurrentStatus, " +
            "FUNCTION('DATEDIFF', l.dueDate, current date)" +
            "FROM Loan l " +
            "JOIN l.customer c " +
            "JOIN l.product p " +
            "LEFT JOIN l.user u " +
            "WHERE l.dueDate BETWEEN :fromDate AND :toDate " +
            "and l.loanStatus in (:status)" +
            "AND (:search IS NULL " +
            "OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            ")" +
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or l.branch.id = :branchId)")
    Page<Tuple> getDueLoansByRange(LocalDate fromDate, LocalDate toDate, int userId, String search, List<LoanStatus> status, String role, Integer branchId, Pageable pageable);

    List<Loan> user(User user);

    @Query("select count(*) " +
            "from Loan l join l.user u " +
            "where l.dueDate = :day " +
            "and l.loanStatus in (:status) " +
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or l.branch.id = :branchId)")
    Long duesCount(int userId, LocalDate day, List<LoanStatus>status, Integer branchId, String role);

    @Query("select count(*) " +
            "from Loan l " +
            "LEFT JOIN l.user u " +
            "where l.dueDate between :from and :to " +
            "and l.loanStatus in (:status)" +
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or l.branch.id = :branchId)")
    Long duesRangeCount(int userId, LocalDate from, LocalDate to, List<LoanStatus> status, Integer branchId, String role);

    @Query("select count(*) " +
            "from Loan l join l.user u " +
            "where l.loanStatus in (:status) " +
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or l.branch.id = :branchId)")
    Long loansWithStatusCount(int userId, List<LoanStatus> status, Integer branchId, String role);

    @Query("select count(*) " +
            "from Loan l " +
            "LEFT JOIN l.user u " +
            "where l.approvalStatus = :approvalStatus " +
            "AND (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or l.branch.id = :branchId)")
    Long loansWithApprovalStatusCount(int userId, String role, Integer branchId, ApprovalStatus approvalStatus);

    @Query("select coalesce(sum(l.interest), 0) " +
            "from Loan l " +
            "LEFT JOIN l.user u " +
            "where l.loanStatus = :status " +
            "and month(l.disbursementDate) = :month "+
            "AND (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or l.branch.id = :branchId)")
    float totalInterest(int userId, LoanStatus status, String role, Integer branchId, Integer month);

    @Query("select coalesce(sum(l.interest), 0) " +
            "from Loan l " +
            "LEFT JOIN l.user u " +
            "where l.loanStatus = :status " +
            "and l.disbursementDate = :date "+
            "AND (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or l.branch.id = :branchId)")
    float interestEarnedToday(int userId, LoanStatus status, String role, Integer branchId, LocalDate date);

    @Query("select " +
            "coalesce(sum(case when l.paidAmount >= l.principal " +
            "then (l.paidAmount - l.principal) else 0 end), 0) " +
            "from Loan l " +
            "LEFT JOIN l.user u " +
            "where l.loanStatus in (:status) " +
            "and month(l.disbursementDate) = :month " +
            "AND (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or l.branch.id = :branchId)")
    float interestPaid(int userId, List<LoanStatus> status, String role, Integer branchId, Integer month);

    @Query("SELECT coalesce(sum(l.principal), 0) " +
            "FROM Loan l " +
            "LEFT JOIN l.user u " +
            "WHERE (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or l.branch.id = :branchId)" +
            "AND l.loanStatus = :status " +
            "AND l.disbursementDate = :day ")
    float dayDisbursedAmount(int userId, LoanStatus status, String role, Integer branchId, LocalDate day);

    @Query("SELECT coalesce(sum(l.principal), 0) " +
            "FROM Loan l " +
            "LEFT JOIN l.user u " +
            "WHERE (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or l.branch.id = :branchId) " +
            "AND l.loanStatus = :status " +
            "AND MONTH(l.disbursementDate) = :month ")
    float totalMonthlyDisbursement (int userId, LoanStatus status, String role, Integer branchId, Integer month);

    @Query("select l from Loan l where l.customer.id = :customerId order by l.id desc limit 1")
    Loan findLatestByCustomerId(Integer customerId);

    @Query("select l from Loan l where l.id = :loanId and l.loanStatus in (:status)")
    Loan findLoan(int loanId, List<LoanStatus> status);

    @Query("select l from Loan l where l.customer.id = :customerId and l.loanStatus in (:status)")
    List<Loan> checkLoanExistence(Integer customerId, List<LoanStatus> status);

    @Query("select l from Loan l where l.id = :loanId " +
            "and l.loanStatus in (:statuses)")
    Loan getRollOverLoan(int loanId, List<LoanStatus> statuses);

    @Query("SELECT l.id, CONCAT(c.firstName, ' ', c.lastName), l.principal, l.processingFee," +
         "l.interest, l.totalAmount, l.paidAmount, l.remainingBalance, c.monthlyIncome, " +
         "l.installmentAmount, l.arrears, l.installmentType, c.phone, l.purpose, l.loanStatus, l.loanCurrentStatus, p.name, " +
         "u.firstName, l.dueDate , FUNCTION('DATEDIFF', l.dueDate, current date)," +
         "l.defaultDate, l.applicationDate, l.appliedAmount, l.approvalDate, l.disbursementDate, l.updatedAt " +
         "FROM Loan l " +
         "JOIN l.customer c " +
         "JOIN l.product p " +
         "JOIN l.user u " +
         "where l.id = :id"
    )
    Tuple findLoanDetailsById(int id);

    @Query(value = """
    SELECT
        EXTRACT(YEAR FROM l.disbursement_date) AS year,
        EXTRACT(MONTH FROM l.disbursement_date) AS month,
        SUM(l.principal) AS sum
    FROM loans l
    LEFT JOIN users u ON l.officer_id = u.id
    WHERE
        l.disbursement_date IS NOT NULL
        AND l.disbursement_date >= CURRENT_DATE - INTERVAL 12 MONTH
        AND (:role = 'admin' OR u.id = :userId)
        and (:branchId is null or l.branch_id = :branchId)
    GROUP BY
        EXTRACT(YEAR FROM l.disbursement_date),
        EXTRACT(MONTH FROM l.disbursement_date)
    HAVING SUM(l.principal) > 0
    ORDER BY
        year, month
""", nativeQuery = true)
    List<MonthlyTrendProjection> findRolling12MonthDisbursementTrends(
            String role,
            Integer userId,
            Integer branchId
    );



    @Query("select l.id, concat(c.firstName, ' ', c.lastName), l.principal, l.totalAmount, l.interest, " +
            "l.processingFee, l.disbursementDate, t.transactionCode, c.phone " +
            "from Loan l " +
            "left join User u on u.id = l.user.id " +
            "join Customer c on c.id = l.customer.id " +
            "join Transaction t on l.id = t.loan.id " +
            "where l.disbursementDate between :startDate and :endDate " +
            "and t.type = 'disbursement' " +
            "and (:role = 'admin' OR u.id = :officerId)")
    List<Tuple> getDisbursementsByOfficerIdAndRange(Integer officerId, LocalDate startDate, String role, LocalDate endDate);

    boolean existsByCustomerAndLoanStatusNotIn(Customer customer, List<LoanStatus> paid);
}
