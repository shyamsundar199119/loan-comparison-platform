package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.service.LoanOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@RequiredArgsConstructor
public class LoanOfferController {

    private final LoanOfferService loanOfferService;
    public ResponseEntity<LoanOfferResponse> acceptLoanOffer(UUID applicationId, UUID offerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
