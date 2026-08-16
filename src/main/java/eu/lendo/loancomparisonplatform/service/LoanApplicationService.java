package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;

    public LoanApplicationResponse createLoanApplication(LoanApplicationRequest request) {
        Instant now = Instant.now();

        LoanApplication loanApplication = LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicantFirstName(request.firstName())
                .applicantLastName(request.lastName())
                .applicantEmail(request.emailId())
                .requestLoanAmount(request.requestAmount())
                .requestLoanTermMonths(request.loanTermMonths())
                .status(ApplicationStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusSeconds(86400)) // Example: 1 day processing time
                .build();

        loanApplicationRepository.save(loanApplication);

        return new LoanApplicationResponse(
                loanApplication.getId(),
                loanApplication.getApplicantFirstName(),
                loanApplication.getApplicantLastName(),
                loanApplication.getApplicantEmail(),
                loanApplication.getRequestLoanAmount(),
                loanApplication.getRequestLoanTermMonths(),
                loanApplication.getExpiresAt(),
                loanApplication.getCreatedAt(),
                loanApplication.getStatus(),
                List.of() // No loan offers in this case
        );
    }

    public LoanApplicationResponse getLoanApplication(UUID applicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        return new LoanApplicationResponse(
                loanApplication.getId(),
                loanApplication.getApplicantFirstName(),
                loanApplication.getApplicantLastName(),
                loanApplication.getApplicantEmail(),
                loanApplication.getRequestLoanAmount(),
                loanApplication.getRequestLoanTermMonths(),
                loanApplication.getExpiresAt(),
                loanApplication.getCreatedAt(),
                loanApplication.getStatus(),
                List.of() // No loan offers logic initially - to be implemented
        );
    }

    public List<LoanApplicationResponse> listLoanApplications(ApplicationStatus status, Instant from, Instant to) {
        List<LoanApplication> loanApplications = loanApplicationRepository.findAllByFilters(status, from, to);

        return loanApplications.stream()
                .map(loanApplication -> new LoanApplicationResponse(
                        loanApplication.getId(),
                        loanApplication.getApplicantFirstName(),
                        loanApplication.getApplicantLastName(),
                        loanApplication.getApplicantEmail(),
                        loanApplication.getRequestLoanAmount(),
                        loanApplication.getRequestLoanTermMonths(),
                        loanApplication.getExpiresAt(),
                        loanApplication.getCreatedAt(),
                        loanApplication.getStatus(),
                        List.of() // No loan offers logic initially
                ))
                .collect(Collectors.toList());
    }
}
