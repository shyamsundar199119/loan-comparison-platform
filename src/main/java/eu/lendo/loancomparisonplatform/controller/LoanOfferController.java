package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.service.LoanOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/application/{applicationId}/offers")
@RequiredArgsConstructor
public class LoanOfferController {

    private final LoanOfferService loanOfferService;

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<LoanOfferResponse> acceptLoanOffer(@PathVariable UUID applicationId, @PathVariable UUID offerId) {
        return ResponseEntity.ok(loanOfferService.acceptLoanOffer(applicationId, offerId));
    }
}
