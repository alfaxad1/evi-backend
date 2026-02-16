package com.example.loanApp.repository;

import com.example.loanApp.entities.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository <LoanProduct, Integer> {
    @Query("select p from LoanProduct p " +
            "where p.isActive = true")
    List<LoanProduct> findAllProducts();
}
