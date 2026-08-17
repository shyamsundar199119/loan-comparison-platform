package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationExpirationSchedulerTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private LoanApplicationExpirationScheduler expirationScheduler;

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-08-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
        expirationScheduler = new LoanApplicationExpirationScheduler(loanApplicationRepository, fixedClock);
    }

    @Test
    void expireApplications_whenExpiredPendingApplicationsExist_marksThemAsExpired() {

        LoanApplication application1 = createApplication(ApplicationStatus.PENDING, now.minusSeconds(7200));
        LoanApplication application2 = createApplication(ApplicationStatus.PENDING, now.minusSeconds(3600));

        when(loanApplicationRepository.findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now)).thenReturn(List.of(application1, application2));
        expirationScheduler.expireApplications();

        assertThat(application1.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
        assertThat(application2.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
        verify(loanApplicationRepository).findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now);
    }

    @Test
    void expireApplications_whenNoExpiredApplicationsExist_doesNothing() {
        when(loanApplicationRepository.findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now)).thenReturn(List.of());
        expirationScheduler.expireApplications();
        verify(loanApplicationRepository).findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now);
    }

    @Test
    void expireApplications_whenApplicationIsAlreadyAccepted_doesNotChangeIt() {
        LoanApplication application = createApplication(ApplicationStatus.ACCEPTED, now.minusSeconds(3600));
        when(loanApplicationRepository.findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now)).thenReturn(List.of());
        expirationScheduler.expireApplications();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(loanApplicationRepository).findAllByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now);
    }

    private LoanApplication createApplication(ApplicationStatus status, Instant expiresAt) {
        return LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicantFirstName("Shyam")
                .applicantLastName("Sundar")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(250000))
                .requestLoanTermMonths(120)
                .status(status)
                .createdAt(expiresAt.minusSeconds(86400))
                .expiresAt(expiresAt)
                .build();
    }
}
