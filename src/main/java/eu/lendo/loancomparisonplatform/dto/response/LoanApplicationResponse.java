package eu.lendo.loancomparisonplatform.dto.response;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LoanApplicationResponse(
        String applicationId,
        String applicantFirstName,
        String applicantLastName,
        String applicantEmail,
        BigDecimal loanAmount,
        Integer loanTermMonths,
        Instant expiresAt,
        Instant createdAt,
        ApplicationStatus status,
        List<LoanOfferResponse> loanOffers
) {
}
