package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/application")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createApplication(@Valid @RequestBody LoanApplicationRequest request) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationResponse> getApplication(@PathVariable UUID applicationId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> listApplications(@RequestParam(required = false) ApplicationStatus status,
                                                                          @RequestParam(required = false) Instant from,
                                                                          @RequestParam(required = false) Instant to) {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
