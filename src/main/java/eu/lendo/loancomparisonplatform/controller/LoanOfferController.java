package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.dto.request.LoanOfferRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.service.LoanOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/application/{applicationId}/offers")
@RequiredArgsConstructor
public class LoanOfferController implements LoanOfferApi {

    private final LoanOfferService loanOfferService;

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<LoanOfferResponse> acceptLoanOffer(@PathVariable UUID applicationId, @PathVariable UUID offerId) {
        return ResponseEntity.ok(loanOfferService.acceptLoanOffer(applicationId, offerId));
    }

    @PostMapping
    public ResponseEntity<LoanOfferResponse> saveLoanOffer(@PathVariable UUID applicationId, @Valid @RequestBody LoanOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanOfferService.createLoanOffer(applicationId, request));
    }
}
