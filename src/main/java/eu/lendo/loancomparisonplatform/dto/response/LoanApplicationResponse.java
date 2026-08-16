package eu.lendo.loancomparisonplatform.dto.response;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoanApplicationResponse(
        UUID applicationId,
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
