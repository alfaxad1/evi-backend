package com.example.loanApp.repository;

import com.example.loanApp.entities.CustomerCollateral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerCollateralRepository extends JpaRepository<CustomerCollateral, Integer> {
}
