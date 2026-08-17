package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.dto.request.LoanOfferRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationStateException;
import eu.lendo.loancomparisonplatform.exception.LoanOfferNotFoundException;
import eu.lendo.loancomparisonplatform.exception.LoanOfferStateException;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import eu.lendo.loancomparisonplatform.repository.LoanOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.domain.LoanOffer;
import eu.lendo.loancomparisonplatform.domain.OfferStatus;
import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanOfferService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanOfferRepository loanOfferRepository;

    public LoanOfferResponse createLoanOffer(UUID applicationId, LoanOfferRequest request){
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Transactional
    public LoanOfferResponse acceptLoanOffer(UUID applicationId, UUID offerId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                        .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        LoanOffer offer = loanOfferRepository.findById(offerId).orElseThrow(() -> new LoanOfferNotFoundException("Loan offer not found: " +offerId));

        validateOfferBelongsToApplication(application, offer);
        validateApplicationCanAcceptOffer(application);
        validateOfferCanBeAccepted(offer);

        // Accept selected offer
        offer.setStatus(OfferStatus.ACCEPTED);

        // Accept application
        application.setStatus(ApplicationStatus.ACCEPTED);

        // Reject all other offers
        application.getLoanOffers().stream().filter(otherOffer -> !otherOffer.getId().equals(offerId))
                .forEach(otherOffer -> otherOffer.setStatus(OfferStatus.REJECTED));

        return mapToResponse(offer);
    }

    private void validateOfferBelongsToApplication(LoanApplication application, LoanOffer offer) {
        if (!application.getId().equals(offer.getLoanApplication().getId())) {
            throw new LoanOfferNotFoundException("Loan offer not found: " + offer.getId());
        }
    }

    private void validateApplicationCanAcceptOffer(LoanApplication application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new LoanApplicationStateException(
                    "Loan application must be in PENDING status to accept an offer"
            );
        }
    }

    private void validateOfferCanBeAccepted(LoanOffer offer) {
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new LoanOfferStateException(
                    "Loan offer must be in PENDING status to be accepted"
            );
        }
    }

    private LoanOfferResponse mapToResponse(LoanOffer offer) {
        return new LoanOfferResponse(
                offer.getId(),
                offer.getLoanApplication().getId(),
                offer.getLenderName(),
                offer.getInterestRate(),
                offer.getMonthlyPayment(),
                offer.getTotalRepaymentAmount(),
                offer.getCreatedAt(),
                offer.getStatus()
        );
    }
}
