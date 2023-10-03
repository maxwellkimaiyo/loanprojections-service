package com.jia.loanprojectionsservice.application.service.loanFeeProjection;

import com.jia.loanprojectionsservice.application.validation.LoanProjectionValidation;
import com.jia.loanprojectionsservice.domain.entities.LoanProductEntity;
import com.jia.loanprojectionsservice.domain.repository.LoanProductRepository;
import com.jia.loanprojectionsservice.infrastructure.controller.request.LoanProjectionRequest;
import com.jia.loanprojectionsservice.infrastructure.controller.response.LoanProjectionResponse;
import com.jia.loanprojectionsservice.infrastructure.controller.response.LoanProjections;
import com.jia.loanprojectionsservice.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jia.loanprojectionsservice.domain.enums.DurationUnitType.WEEKS;
import static com.jia.loanprojectionsservice.domain.enums.LoanTypes.MONTHLY;
import static com.jia.loanprojectionsservice.domain.enums.LoanTypes.WEEKLY;

/**
 * The type loan projection service.
 */
@Service
public class LoanFeeProjectionsServiceImpl implements LoanFeeProjectionsService {

    /**
     * The Logging.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoanFeeProjectionsServiceImpl.class);


    /**
     * The type loan product repository
     */
    final LoanProductRepository loanProductRepository;
    /**
     * The type loan projection validation
     */
    final LoanProjectionValidation loanProjectionValidation;

    @Autowired
    public LoanFeeProjectionsServiceImpl(LoanProductRepository loanProductRepository, LoanProjectionValidation loanProjectionValidation) {
        this.loanProductRepository = loanProductRepository;
        this.loanProjectionValidation = loanProjectionValidation;
    }

    /**
     * Generates loan fee projections based on the provided request.
     *
     * @param request the loan projection request
     * @return a list of loan fee projections
     */

    @Override
    public LoanProjectionResponse getLoanFeeProjections(LoanProjectionRequest request) {
        loanProjectionValidation.validateRequest(request);
        try {
            // Retrieve loan product configuration from loan product entity table
            Optional<LoanProductEntity> productEntity = loanProductRepository.findByLoanType(request.getInstallmentFrequency());
            if (productEntity.isPresent()) {
                List<LoanProjections> feeProjections = generateLoanFeeProjections(request, productEntity.get());
                return new LoanProjectionResponse(feeProjections);
            }
        } catch (Exception e) {
            LOGGER.error("ERROR: {}", e.getMessage(), e);
        }
        return new LoanProjectionResponse();
    }



    /**
     * Generates loan fee projections based on the provided request and loan product configuration.
     *
     * @param request     The request object containing loan details
     * @param loanProduct The product entity configuration specifying fee rates
     * @return The list of loan fee projections
     */

    private List<LoanProjections> generateLoanFeeProjections(LoanProjectionRequest request, LoanProductEntity loanProduct) {
        List<LoanProjections> loanProjections = new ArrayList<>();
        double loanAmount = request.getLoanAmount();
        double loanInterestRate = loanProduct.getLoanFee().getInterestRate();
        double serviceInterestRate = loanProduct.getLoanFee().getServiceFeeRate();
        double serviceFeeCap = loanProduct.getLoanFee().getServiceFeeCap();
        LocalDate startDate = DateUtil.getDate(request.getStartDate());
        // Loan durations specified in either weeks or months
        int loanDuration = request.getLoanDuration();
        // Counter to keep track of the number of months since the last service fee calculation
        int monthsSinceLastServiceFee = 0;
        // Check if the installment frequency is weekly
        boolean weeklyInstallment = WEEKLY.name().equalsIgnoreCase(request.getInstallmentFrequency());
        // Check if the loan duration is specified in weeks and needs to be calculated with weekly installments
        if (WEEKS.name().equalsIgnoreCase(request.getLoanDurationUnit()) && MONTHLY.name().equalsIgnoreCase(request.getInstallmentFrequency())) {
            loanDuration *= 4;
        }

        // Iterate through each month or week of the loan duration
        for (int duration = 1; duration <= loanDuration; duration++) {
            LocalDate date = weeklyInstallment ? startDate.plusWeeks(duration - 1) : startDate.plusMonths(duration - 1);

            // Calculate interest for the duration (1% or 4% of the principal amount)
            double interestAmount = (loanInterestRate / 100) * loanAmount;

            if (weeklyInstallment && duration % 2 == 0) {
                // Calculate service fee every 2 weeks, which is 0.5% of the principal amount with a maximum cap of $50
                double serviceFee = (serviceInterestRate / 100) * loanAmount;
                serviceFee = Math.min(serviceFeeCap, serviceFee);
                loanProjections.add(new LoanProjections(date, serviceFee));
            } else if (!weeklyInstallment && monthsSinceLastServiceFee == 3) {
                // Calculate service fee every 3 months (0.5% of the principal with a maximum cap of $100)
                double serviceFee = (serviceInterestRate / 100) * loanAmount;
                serviceFee = Math.min(serviceFeeCap, serviceFee);
                // Add the calculated service fee to the list of projections
                loanProjections.add(new LoanProjections(date, serviceFee));
                monthsSinceLastServiceFee = 0;
            }

            // Increment the count of months since the last service fee calculation
            if (!weeklyInstallment) {
                monthsSinceLastServiceFee++;
            }
            // Add the calculated interest fee
            loanProjections.add(new LoanProjections(date, interestAmount));
        }

        return loanProjections;
    }



}