package eu.lendo.loancomparisonplatform.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "applicant_first_name", nullable = false, length = 100)
    private String applicantFirstName;

    @Column(name = "applicant_last_name", nullable = false, length = 100)
    private String applicantLastName;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    // precision = 15, scale = 2 supports amounts up to 9,999,999,999,999.99
    @Column(name = "requested_loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestLoanAmount;

    @Column(name = "requested_loan_term_months", nullable = false)
    private Integer requestLoanTermMonths;

    @Enumerated(EnumType.STRING) // Saves enum text (e.g., 'PENDING') instead of integers (0, 1)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Maps the relationship to your LoanOffer entity
    // "loanApplication" must match the variable name inside your LoanOffer class
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoanOffer> loanOffers = new ArrayList<>();

}

