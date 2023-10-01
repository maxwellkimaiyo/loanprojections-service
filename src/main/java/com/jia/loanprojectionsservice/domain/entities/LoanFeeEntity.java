package com.jia.loanprojectionsservice.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "loan_fee")
public class LoanFeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private double interestRate;
    private double serviceFeeRate;
    private double serviceFeeCap;


}