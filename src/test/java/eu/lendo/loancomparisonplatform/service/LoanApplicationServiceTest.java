package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private LoanApplicationRequest request;

    @BeforeEach
    void setUp() {
        request = new LoanApplicationRequest("Shyam Sundar", "Durai Pandian", "shyam@example.com", BigDecimal.valueOf(250_000), 120);
    }

    @Test
    void createLoanApplication_withValidRequest_returnsCreatedApplication() {
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);

        assertThat(response).isNotNull();
        assertThat(response.applicationId()).isNotNull();
        assertThat(response.applicantFirstName()).isEqualTo("Shyam Sundar");
        assertThat(response.applicantLastName()).isEqualTo("Durai Pandian");
        assertThat(response.applicantEmail()).isEqualTo("shyam@example.com");
        assertThat(response.loanAmount()).isEqualByComparingTo("250000");
        assertThat(response.loanTermMonths()).isEqualTo(120);
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);

        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    void getLoanApplication_whenApplicationExists_returnsApplication() {

        UUID applicationId = UUID.randomUUID();

        LoanApplication application = LoanApplication.builder()
                .id(applicationId)
                .applicantFirstName("Shyam Sundar")
                .applicantLastName("Durai Pandian")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(250_000))
                .requestLoanTermMonths(120)
                .status(ApplicationStatus.PENDING)
                .createdAt(Instant.now()).build();

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        LoanApplicationResponse response = loanApplicationService.getLoanApplication(applicationId);

        assertThat(response).isNotNull();
        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);

        verify(loanApplicationRepository).findById(applicationId);
    }

    @Test
    void getLoanApplication_whenApplicationDoesNotExist_throwsNotFoundException() {
        UUID applicationId = UUID.randomUUID();
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> loanApplicationService.getLoanApplication(applicationId)).isInstanceOf(LoanApplicationNotFoundException.class);
        verify(loanApplicationRepository).findById(applicationId);
    }

    @Test
    void listLoanApplications_withoutFilters_returnsApplications() {

        List<LoanApplication> applications = List.of(createApplication(ApplicationStatus.PENDING), createApplication(ApplicationStatus.ACCEPTED));
        when(loanApplicationRepository.findAllByFilters(isNull(), isNull(), isNull())).thenReturn(applications);
        List<LoanApplicationResponse> response = loanApplicationService.listLoanApplications(null, null, null);
        assertThat(response).hasSize(2);
        verify(loanApplicationRepository).findAllByFilters(null, null, null);
    }

    @Test
    void listLoanApplications_withStatusFilter_returnsMatchingApplications() {
        List<LoanApplication> applications = List.of(createApplication(ApplicationStatus.PENDING));
        when(loanApplicationRepository.findAllByFilters(eq(ApplicationStatus.PENDING), isNull(), isNull())).thenReturn(applications);
        List<LoanApplicationResponse> response = loanApplicationService.listLoanApplications(ApplicationStatus.PENDING, null, null);
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().status()).isEqualTo(ApplicationStatus.PENDING);
        verify(loanApplicationRepository).findAllByFilters(ApplicationStatus.PENDING, null, null);
    }

    @Test
    void listLoanApplications_withDateRangeFilter_returnsMatchingApplications() {

        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-10T23:59:59Z");

        List<LoanApplication> applications = List.of(createApplication(ApplicationStatus.PENDING));
        when(loanApplicationRepository.findAllByFilters(isNull(), eq(from), eq(to))).thenReturn(applications);
        List<LoanApplicationResponse> response = loanApplicationService.listLoanApplications(null, from, to);
        assertThat(response).hasSize(1);
        verify(loanApplicationRepository).findAllByFilters(null, from, to);
    }

    @Test
    void listLoanApplications_withAllFilters_returnsMatchingApplications() {

        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-10T23:59:59Z");

        List<LoanApplication> applications = List.of(createApplication(ApplicationStatus.PENDING));
        when(loanApplicationRepository.findAllByFilters(eq(ApplicationStatus.PENDING), eq(from), eq(to))).thenReturn(applications);
        List<LoanApplicationResponse> response = loanApplicationService.listLoanApplications(ApplicationStatus.PENDING, from, to);
        assertThat(response).hasSize(1);
        verify(loanApplicationRepository).findAllByFilters(ApplicationStatus.PENDING, from, to);
    }

    // ---------- Repository failure tests ----------

    @Test
    void createLoanApplication_whenRepositoryFails_propagatesException() {

        DataAccessException repositoryException = new DataAccessException("Database unavailable") {
        };

        when(loanApplicationRepository.save(any(LoanApplication.class))).thenThrow(repositoryException);
        assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request)).isSameAs(repositoryException);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    void getLoanApplication_whenRepositoryFails_propagatesException() {

        UUID applicationId = UUID.randomUUID();
        DataAccessException repositoryException = new DataAccessException("Database unavailable") {
        };

        when(loanApplicationRepository.findById(applicationId)).thenThrow(repositoryException);
        assertThatThrownBy(() -> loanApplicationService.getLoanApplication(applicationId)).isSameAs(repositoryException);
        verify(loanApplicationRepository).findById(applicationId);
    }

    @Test
    void listLoanApplications_whenRepositoryFails_propagatesException() {

        DataAccessException repositoryException = new DataAccessException("Database unavailable") {
        };

        when(loanApplicationRepository.findAllByFilters(any(), any(), any())).thenThrow(repositoryException);
        assertThatThrownBy(() -> loanApplicationService.listLoanApplications(ApplicationStatus.PENDING, null, null)).isSameAs(repositoryException);
        verify(loanApplicationRepository).findAllByFilters(ApplicationStatus.PENDING, null, null);
    }

    private LoanApplication createApplication(ApplicationStatus status) {
        return LoanApplication.builder().id(UUID.randomUUID())
                .applicantFirstName("Shyam Sundar")
                .applicantLastName("Durai Pandian")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(250_000))
                .requestLoanTermMonths(120).status(status).createdAt(Instant.now()).build();
    }
}