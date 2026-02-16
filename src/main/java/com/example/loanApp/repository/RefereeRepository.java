package com.example.loanApp.repository;

import com.example.loanApp.entities.Referee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Integer> {
}
