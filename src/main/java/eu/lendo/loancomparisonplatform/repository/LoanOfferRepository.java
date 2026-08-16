package eu.lendo.loancomparisonplatform.repository;

import eu.lendo.loancomparisonplatform.domain.LoanOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanOfferRepository extends JpaRepository<LoanOffer, UUID> {
    List<LoanOffer> findByLoanApplicationId(UUID loanApplicationId);
}
