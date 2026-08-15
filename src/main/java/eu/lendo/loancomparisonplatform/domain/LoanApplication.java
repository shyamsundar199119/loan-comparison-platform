package eu.lendo.loancomparisonplatform.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
public class LoanApplication {

    private UUID id;
    private String applicantFirstName;
    private String applicantLastName;
    private String applicantEmail;
    private BigDecimal requestLoanAmount;
    private Integer requestLoanTermMonths;
    private ApplicationStatus status;
    private Instant expiresAt;
    private Instant createdAt;

    @Builder.Default
    private List<LoanOffer> loanOffers = new ArrayList<>();

}
