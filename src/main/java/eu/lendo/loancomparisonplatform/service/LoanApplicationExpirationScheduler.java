package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationExpirationScheduler {

    private final LoanApplicationRepository loanApplicationRepository;
    private final Clock clock;

    @Transactional
    @Scheduled(fixedDelayString = "${loan.application.expiration-check-interval:60000}")
    public void expireApplications() {
        Instant now = Instant.now(clock);
        List<LoanApplication> expiredApplications = loanApplicationRepository.findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now);
        expiredApplications.forEach(application -> application.setStatus(ApplicationStatus.EXPIRED));
    }
}
