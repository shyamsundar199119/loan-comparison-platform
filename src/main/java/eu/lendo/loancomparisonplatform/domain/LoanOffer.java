package eu.lendo.loancomparisonplatform.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
public class LoanOffer {

    private UUID id;
    private UUID loanApplicationId;
    private String lenderName;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private Integer loanTermMonths;
    private BigDecimal totalRepaymentAmount;
    private OfferStatus status;
    private Instant createdAt;
}
