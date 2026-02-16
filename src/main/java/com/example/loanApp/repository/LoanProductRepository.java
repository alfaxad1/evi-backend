package com.example.loanApp.repository;

import com.example.loanApp.entities.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct,Integer> {
}
