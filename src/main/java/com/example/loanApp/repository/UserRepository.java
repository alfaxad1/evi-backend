package com.example.loanApp.repository;

import com.example.loanApp.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    User findUserById(Integer id);

    @Query("select u from User u " +
            "where u.isActive = :isActive " +
            "and (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) ) ")
    Page<User> findActiveUsers(@Param("search") String search, Pageable pageable, boolean isActive);

    @Query("select role from User where id = :userId")
    String findRoleById(int userId);

//    @Query("SELECT u.firstName, u.lastName, " +
//            "COUNT(l.id) AS loanCount, " +
//            "COALESCE(SUM(l.principal), 0) AS totalAmount " +
//            "FROM User u " +
//            "JOIN Loan l " +
//            "WHERE u.role =: role " +
//            "AND MONTH(l.disbursementDate) =: month " +
//            "AND YEAR(l.disbursementDate) =: year " +
//            "ORDER BY u.id, u.firstName, u.lastName")
//    Page<Loan> getOfficerMonthlyDisbursements(String month, Long year, Pageable pageable);
}
