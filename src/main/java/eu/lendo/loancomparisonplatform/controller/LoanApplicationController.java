package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.exception.InvalidDateRangeException;
import eu.lendo.loancomparisonplatform.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/application")
public class LoanApplicationController implements LoanApplicationApi{

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createApplication(@Valid @RequestBody LoanApplicationRequest request) {
        return new ResponseEntity<>(loanApplicationService.createLoanApplication(request), HttpStatus.CREATED);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationResponse> getApplication(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplication(applicationId));
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> listApplications(@RequestParam(required = false) ApplicationStatus status,
                                                                          @RequestParam(required = false) Instant from,
                                                                          @RequestParam(required = false) Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDateRangeException("'from' must not be after 'to'");
        }
        return ResponseEntity.ok(loanApplicationService.listLoanApplications(status, from, to));
    }

}
