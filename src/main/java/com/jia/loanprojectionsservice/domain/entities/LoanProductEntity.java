package com.jia.loanprojectionsservice.domain.entities;

import com.jia.loanprojectionsservice.domain.enums.LoanTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "loan_product")
public class LoanProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LoanTypes type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "loan_duration_id", referencedColumnName = "id")
    private LoanDurationEntity loanDuration;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "installment_frequency_id", referencedColumnName = "id")
    private InstallmentFrequencyEntity installmentFrequency;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "loan_fee_id", referencedColumnName = "id")
    private LoanFeeEntity loanFee;

}