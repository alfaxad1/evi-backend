package com.example.loanApp.repository;

import com.example.loanApp.entities.GuarantorCollateral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GurantorCollateralRepository extends JpaRepository<GuarantorCollateral, Integer> {
}
