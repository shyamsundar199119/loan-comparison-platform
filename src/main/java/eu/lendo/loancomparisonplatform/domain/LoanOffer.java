package eu.lendo.loancomparisonplatform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_offers")
@Builder
@Getter
@Setter
@NoArgsConstructor  // Required by JPA for entity instantiation
@AllArgsConstructor // Required by Lombok's @Builder
public class LoanOffer {

    @Id
    @GeneratedValue // Generates UUID automatically for PostgreSQL
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Defines the foreign key relationship to the LoanApplication entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "lender_name", nullable = false, length = 150)
    private String lenderName;

    // precision = 5, scale = 2 supports rates up to 999.99% (e.g., 5.45%)
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "monthly_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "total_repayment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepaymentAmount;

    @Enumerated(EnumType.STRING) // Saves enum text (e.g., 'ACCEPTED') in PostgreSQL
    @Column(name = "status", nullable = false, length = 50)
    private OfferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

