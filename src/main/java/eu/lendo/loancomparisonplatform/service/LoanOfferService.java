package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.repository.LoanOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanOfferService {

    private final LoanOfferRepository loanOfferRepository;
    public LoanOfferResponse acceptLoanOffer(UUID applicationId, UUID offerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
