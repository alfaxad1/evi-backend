package com.example.loanApp.repository;

import com.example.loanApp.entities.Lead;
import com.example.loanApp.enums.LeadStatus;
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
public interface LeadRepository extends JpaRepository<Lead, Integer> {

    /**
     * Leads list. Ordering puts follow-ups that are due/overdue first
     * (null next action dates last), then the most recent leads.
     */
    @Query("""
            select l from Lead l
            where (:branchId is null or l.branch.id = :branchId)
              and (:officerId is null or l.assignedOfficer.id = :officerId)
              and (:status is null or l.status = :status)
              and (:dueBefore is null or (l.nextActionDate is not null and l.nextActionDate <= :dueBefore))
              and (:search is null
                   or lower(l.name) like lower(concat('%', :search, '%'))
                   or lower(l.phoneNumber) like lower(concat('%', :search, '%')))
            order by
              case when l.status = com.example.loanApp.enums.LeadStatus.OPEN
                     or l.status = com.example.loanApp.enums.LeadStatus.CONTACTED then 0 else 1 end asc,
              case when l.nextActionDate is null then 1 else 0 end asc,
              l.nextActionDate asc,
              l.createdAt desc
            """)
    Page<Lead> search(@Param("branchId") Integer branchId,
                      @Param("officerId") Integer officerId,
                      @Param("status") LeadStatus status,
                      @Param("dueBefore") LocalDate dueBefore,
                      @Param("search") String search,
                      Pageable pageable);

    long countByStatus(LeadStatus status);

    @Query("""
            select count(l) from Lead l
            where l.status in (com.example.loanApp.enums.LeadStatus.OPEN,
                               com.example.loanApp.enums.LeadStatus.CONTACTED)
              and l.nextActionDate = :day
            """)
    long countDueOn(@Param("day") LocalDate day);

    @Query("""
            select count(l) from Lead l
            where l.status in (com.example.loanApp.enums.LeadStatus.OPEN,
                               com.example.loanApp.enums.LeadStatus.CONTACTED)
              and l.nextActionDate < :day
            """)
    long countOverdue(@Param("day") LocalDate day);

    @Query("""
            select count(l) from Lead l
            where l.status = com.example.loanApp.enums.LeadStatus.CONVERTED
              and l.convertedAt >= :from
            """)
    long countConvertedSince(@Param("from") LocalDateTime from);

    List<Lead> findByPhoneNumber(String phoneNumber);
}
