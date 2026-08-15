package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LoanApplicationService {
    public LoanApplicationResponse createLoanApplication(LoanApplicationRequest request) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public LoanApplicationResponse getLoanApplication(UUID applicationId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public List<LoanApplicationResponse> listLoanApplications(ApplicationStatus status, Instant from, Instant to) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
