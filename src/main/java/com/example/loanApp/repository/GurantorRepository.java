package com.example.loanApp.repository;

import com.example.loanApp.entities.Guarantor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GurantorRepository extends JpaRepository<Guarantor, Integer> {
}
