package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.domain.LoanOffer;
import eu.lendo.loancomparisonplatform.domain.OfferStatus;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationStateException;
import eu.lendo.loancomparisonplatform.exception.LoanOfferNotFoundException;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import eu.lendo.loancomparisonplatform.repository.LoanOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoanOfferServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanOfferRepository loanOfferRepository;

    @InjectMocks
    private LoanOfferService loanOfferService;

    private UUID applicationId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        offerId = UUID.randomUUID();
    }

    @Test
    void acceptLoanOffer_whenApplicationAndOfferArePending_acceptsOffer() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);
        LoanOffer offer = createOffer(offerId, application, OfferStatus.PENDING);

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.findById(offerId)).thenReturn(Optional.of(offer));

        LoanOfferResponse response = loanOfferService.acceptLoanOffer(applicationId, offerId);

        assertThat(response).isNotNull();
        assertThat(response.offerId()).isEqualTo(offerId);
        assertThat(response.status()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(offer.getStatus()).isEqualTo(OfferStatus.ACCEPTED);

        verify(loanApplicationRepository).findById(applicationId);
        verify(loanOfferRepository).findById(offerId);
    }

    @Test
    void acceptLoanOffer_whenApplicationDoesNotExist_throwsApplicationNotFoundException() {
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> loanOfferService.acceptLoanOffer(applicationId, offerId)).isInstanceOf(LoanApplicationNotFoundException.class);

        verify(loanApplicationRepository).findById(applicationId);
        verifyNoInteractions(loanOfferRepository);
    }

    @Test
    void acceptLoanOffer_whenOfferDoesNotExist_throwsLoanOfferNotFoundException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.findById(offerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanOfferService.acceptLoanOffer(applicationId, offerId)).isInstanceOf(LoanOfferNotFoundException.class);

        verify(loanApplicationRepository).findById(applicationId);
        verify(loanOfferRepository).findById(offerId);
    }

    @Test
    void acceptLoanOffer_whenApplicationIsAlreadyAccepted_throwsStateException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.ACCEPTED);
        LoanOffer offer = createOffer(offerId, application, OfferStatus.PENDING);

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> loanOfferService.acceptLoanOffer(applicationId, offerId)).isInstanceOf(LoanApplicationStateException.class);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(offer.getStatus()).isEqualTo(OfferStatus.PENDING);
    }

    @Test
    void acceptLoanOffer_whenApplicationIsExpired_throwsStateException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.EXPIRED);
        LoanOffer offer = createOffer(offerId, application, OfferStatus.PENDING);

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.findById(offerId)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> loanOfferService.acceptLoanOffer(applicationId, offerId)).isInstanceOf(LoanApplicationStateException.class);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
        assertThat(offer.getStatus()).isEqualTo(OfferStatus.PENDING);
    }

    @Test
    void acceptLoanOffer_whenMultipleOffersExist_acceptsSelectedOfferAndRejectsOthers() {
        UUID offerAId = UUID.randomUUID();
        UUID offerBId = UUID.randomUUID();
        UUID offerCId = UUID.randomUUID();

        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);
        LoanOffer offerA = createOffer(offerAId, application, OfferStatus.PENDING);
        LoanOffer offerB = createOffer(offerBId, application, OfferStatus.PENDING);
        LoanOffer offerC = createOffer(offerCId, application, OfferStatus.PENDING);
        application.setLoanOffers(new ArrayList<>(List.of(offerA, offerB, offerC)));

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.findById(offerBId)).thenReturn(Optional.of(offerB));
        LoanOfferResponse response = loanOfferService.acceptLoanOffer(applicationId, offerBId);
        assertThat(response.offerId()).isEqualTo(offerBId);
        assertThat(response.status()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(offerA.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(offerB.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(offerC.getStatus()).isEqualTo(OfferStatus.REJECTED);
    }

    private LoanApplication createApplication(UUID applicationId, ApplicationStatus status) {
        return LoanApplication.builder().id(applicationId)
                .applicantFirstName("Shyam Sundar")
                .applicantLastName("Durai Pandian")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(250_000))
                .requestLoanTermMonths(120).status(status).createdAt(Instant.now()).build();
    }

    private LoanOffer createOffer(UUID offerId, LoanApplication application, OfferStatus status) {
        return LoanOffer.builder()
                .id(offerId) // Use the provided offerId
                .lenderName("Bank A")
                .loanApplication(application) // Associate with the provided LoanApplication
                .totalRepaymentAmount(BigDecimal.valueOf(300_000)) // Example value
                .interestRate(BigDecimal.valueOf(5.5)) // Example interest rate
                .status(status) // Use the provided status
                .createdAt(Instant.now()) // Set the creation timestamp
                .build();
    }
}
