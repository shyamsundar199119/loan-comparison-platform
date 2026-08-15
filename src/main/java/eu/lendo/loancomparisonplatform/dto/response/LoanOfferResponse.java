package eu.lendo.loancomparisonplatform.dto.response;

import eu.lendo.loancomparisonplatform.domain.OfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanOfferResponse(
        UUID offerId,
        UUID loanApplicationId,
        String lenderName,
        BigDecimal annualInterestRate,
        BigDecimal monthlyPayment,
        BigDecimal totalRepayment,
        Instant createdAt,
        OfferStatus status) {
}
