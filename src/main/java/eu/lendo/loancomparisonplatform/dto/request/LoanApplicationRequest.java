package eu.lendo.loancomparisonplatform.dto.request;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        String firstName,
        String lastName,
        String emailId,
        BigDecimal requestAmount,
        Integer loanTermMonths
) {
}
