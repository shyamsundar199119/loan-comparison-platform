package eu.lendo.loancomparisonplatform.service;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.domain.LoanOffer;
import eu.lendo.loancomparisonplatform.domain.OfferStatus;
import eu.lendo.loancomparisonplatform.dto.request.LoanOfferRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.exception.DuplicateLoanOfferException;
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

    @Test
    void createLoanOffer_whenApplicationIsPending_createsOffer() {

        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);
        LoanOfferRequest request = validLoanOfferRequest();

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.existsByLoanApplicationIdAndLenderName(applicationId, request.lenderName())).thenReturn(false);

        LoanOffer savedOffer = createOffer(application, request);

        when(loanOfferRepository.save(any(LoanOffer.class))).thenReturn(savedOffer);

        LoanOfferResponse response = loanOfferService.createLoanOffer(applicationId, request);

        assertThat(response).isNotNull();
        assertThat(response.lenderName()).isEqualTo("Test Lender");
        assertThat(response.annualInterestRate()).isEqualByComparingTo("5.5");
        assertThat(response.monthlyPayment()).isEqualByComparingTo("2500");
        assertThat(response.totalRepayment()).isEqualByComparingTo("300000");
        assertThat(response.status()).isEqualTo(OfferStatus.PENDING);

        verify(loanApplicationRepository).findById(applicationId);
        verify(loanOfferRepository).existsByLoanApplicationIdAndLenderName(applicationId, request.lenderName());
        verify(loanOfferRepository)
                .save(any(LoanOffer.class));
    }
    @Test
    void createLoanOffer_whenApplicationDoesNotExist_throwsApplicationNotFoundException() {
        LoanOfferRequest request = validLoanOfferRequest();
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> loanOfferService.createLoanOffer(applicationId, request))
                .isInstanceOf(LoanApplicationNotFoundException.class);
        verify(loanApplicationRepository).findById(applicationId);
        verifyNoInteractions(loanOfferRepository);
    }

    @Test
    void createLoanOffer_whenApplicationIsAccepted_throwsLoanApplicationStateException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.ACCEPTED);
        LoanOfferRequest request = validLoanOfferRequest();
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        assertThatThrownBy(() -> loanOfferService.createLoanOffer(applicationId, request)).isInstanceOf(LoanApplicationStateException.class);
        verify(loanApplicationRepository).findById(applicationId);
        verifyNoInteractions(loanOfferRepository);
    }

    @Test
    void createLoanOffer_whenApplicationIsExpired_throwsLoanApplicationStateException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.EXPIRED);
        LoanOfferRequest request = validLoanOfferRequest();
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        assertThatThrownBy(() -> loanOfferService.createLoanOffer(applicationId, request)).isInstanceOf(LoanApplicationStateException.class);
        verify(loanApplicationRepository).findById(applicationId);
        verifyNoInteractions(loanOfferRepository);
    }

    @Test
    void createLoanOffer_whenLenderAlreadyHasOffer_throwsDuplicateLoanOfferException() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);
        LoanOfferRequest request = validLoanOfferRequest();
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.existsByLoanApplicationIdAndLenderName(applicationId, request.lenderName())).thenReturn(true);
        assertThatThrownBy(() -> loanOfferService.createLoanOffer(applicationId, request)).isInstanceOf(DuplicateLoanOfferException.class);
        verify(loanApplicationRepository).findById(applicationId);
        verify(loanOfferRepository).existsByLoanApplicationIdAndLenderName(applicationId, request.lenderName());
        verify(loanOfferRepository, never()).save(any(LoanOffer.class));
    }

    @Test
    void createLoanOffer_whenAnotherLenderHasOffer_createsNewOffer() {
        LoanApplication application = createApplication(applicationId, ApplicationStatus.PENDING);
        LoanOfferRequest request = new LoanOfferRequest("Another Lender", BigDecimal.valueOf(6.0), BigDecimal.valueOf(2600), BigDecimal.valueOf(312000));
        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(loanOfferRepository.existsByLoanApplicationIdAndLenderName(applicationId, request.lenderName())).thenReturn(false);

        LoanOffer savedOffer = createOffer(application, request);
        when(loanOfferRepository.save(any(LoanOffer.class))).thenReturn(savedOffer);

        LoanOfferResponse response = loanOfferService.createLoanOffer(applicationId, request);
        assertThat(response).isNotNull();
        assertThat(response.lenderName()).isEqualTo("Another Lender");
        assertThat(response.status()).isEqualTo(OfferStatus.PENDING);
        verify(loanOfferRepository).save(any(LoanOffer.class));
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

    private LoanOfferRequest validLoanOfferRequest() {
        return new LoanOfferRequest(
                "Test Lender",
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(300000)
        );
    }
    private LoanOffer createOffer(LoanApplication application, LoanOfferRequest request) {
        return LoanOffer.builder()
                .id(UUID.randomUUID())
                .loanApplication(application)
                .lenderName(request.lenderName())
                .interestRate(request.annualInterestRate())
                .monthlyPayment(request.monthlyPaymentAmount())
                .totalRepaymentAmount(request.totalRepaymentAmount())
                .status(OfferStatus.PENDING)
                .build();
    }
}
