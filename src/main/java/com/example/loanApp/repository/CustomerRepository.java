package com.example.loanApp.repository;


import com.example.loanApp.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Customer findByPhone(String phoneNumber);

    boolean existsByPhoneAndNationalId(String phoneNumber, String idNumber);

    @Query("select count(c) from Customer c " +
            "left join User u on u.id = c.user.id " +
            "where (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or c.branch.id = :branchId)")
    Long countActiveCustomers(Boolean isActive, Integer userId,String role, Integer branchId);

    @Query("SELECT COUNT(c) FROM Customer c " +
            "left join User u on u.id = c.user.id " +
            "WHERE (SELECT COUNT(l) FROM Loan l WHERE l.customer = c) > 1 " +
            "and (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or c.branch.id = :branchId)")
    Long countReturnCustomers(String role, Integer userId, Integer branchId);

    @Query("SELECT COUNT(c) FROM Customer c " +
            "left join User u on u.id = c.user.id " +
            "WHERE (SELECT COUNT(l) FROM Loan l WHERE l.customer = c) = 1 " +
            "and (:role = 'admin' OR u.id = :userId)" +
            "and (:branchId is null or c.branch.id = :branchId)")
    Long countOneTimeCustomers(String role, Integer userId, Integer branchId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :start AND c.createdAt < :end")
    Long countCustomersByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Customer c " +
            "left join  c.user u " +
            "WHERE (:role = 'admin' OR u.id = :userId) " +
            "and (:branchId is null or c.branch.id = :branchId) " +
            "AND ( " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.phone LIKE CONCAT('%', :search, '%') " +
            ") ")
    Page<Customer> searchCustomers(
            @Param("userId") Integer userId,
            @Param("search") String search,
            String role,
            Integer branchId,
            Pageable pageable
    );


}
