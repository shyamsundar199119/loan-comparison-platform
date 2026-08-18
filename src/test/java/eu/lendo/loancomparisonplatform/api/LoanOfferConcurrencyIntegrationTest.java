package eu.lendo.loancomparisonplatform.api;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.domain.LoanOffer;
import eu.lendo.loancomparisonplatform.domain.OfferStatus;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import eu.lendo.loancomparisonplatform.repository.LoanOfferRepository;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class LoanOfferConcurrencyIntegrationTest extends BaseApiTest {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private LoanOfferRepository loanOfferRepository;

    @Test
    void acceptLoanOffer_whenTwoOffersAcceptedConcurrently_onlyOneShouldSucceed() throws Exception {

        // Arrange
        LoanApplication application = createPendingApplication();

        LoanOffer firstOffer = createPendingOffer(application, "Lender A");
        LoanOffer secondOffer = createPendingOffer(application, "Lender B");

        // Make sure both entities are committed before starting
        // the concurrent HTTP requests.
        loanOfferRepository.flush();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Response> firstRequest = () -> {
            readyLatch.countDown();
            startLatch.await();
            return acceptOffer(application.getId(), firstOffer.getId());
        };

        Callable<Response> secondRequest = () -> {
            readyLatch.countDown();
            startLatch.await();
            return acceptOffer(application.getId(), secondOffer.getId());
        };

        Future<Response> firstFuture = executorService.submit(firstRequest);
        Future<Response> secondFuture = executorService.submit(secondRequest);

        /*
         * Wait until both requests are ready.
         */
        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();

        /*
         * Release both requests at approximately the same time.
         */
        startLatch.countDown();

        Response firstResponse = getResponse(firstFuture);
        Response secondResponse = getResponse(secondFuture);

        executorService.shutdown();

        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        // Assert exactly one request succeeded
        List<Integer> statusCodes = List.of(firstResponse.statusCode(), secondResponse.statusCode());
        long successfulRequests = statusCodes.stream().filter(status -> status == 200).count();
        assertThat(successfulRequests).as("Exactly one concurrent acceptance request should succeed").isEqualTo(1);

        // The other request should fail with a conflict/state error.
        long failedRequests = statusCodes.stream().filter(status -> status == 422).count();
        assertThat(failedRequests).as("Exactly one concurrent acceptance request should fail").isEqualTo(1);

        // Verify database state
        LoanApplication updatedApplication = loanApplicationRepository.findById(application.getId()).orElseThrow();
        assertThat(updatedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        List<LoanOffer> updatedOffers = loanOfferRepository.findByLoanApplicationId(application.getId());
        long acceptedOffers = updatedOffers.stream().filter(offer -> offer.getStatus() == OfferStatus.ACCEPTED).count();
        long rejectedOffers = updatedOffers.stream().filter(offer -> offer.getStatus() == OfferStatus.REJECTED).count();
        assertThat(acceptedOffers).as("Only one offer can be accepted").isEqualTo(1);
        assertThat(rejectedOffers).as("The other offer should be rejected").isEqualTo(1);
    }

    /**
     * Sends the actual HTTP request used by the concurrency test.
     */
    private Response acceptOffer(UUID applicationId, UUID offerId) {

        return given().contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .pathParam("offerId", offerId)
                .when().post("/api/v1/application/{applicationId}/offers/{offerId}/accept")
                .then().extract().response();
    }

    /**
     * Gets the response from a concurrent request.
     * <p>
     * The Future itself should not fail because the HTTP request
     * is expected to return an HTTP error response rather than
     * throw an exception.
     */
    private Response getResponse(Future<Response> future) throws Exception {
        return future.get(10, TimeUnit.SECONDS);
    }

    /**
     * Creates a PENDING application directly in the database.
     * <p>
     * Using the repository here avoids making the test dependent
     * on the create-application API.
     */
    private LoanApplication createPendingApplication() {
        LoanApplication application = LoanApplication.builder()
                .applicantFirstName("Shyam")
                .applicantLastName("Sundar")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(50_000))
                .requestLoanTermMonths(120)
                .status(ApplicationStatus.PENDING)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86_400))
                .build();
        return loanApplicationRepository.saveAndFlush(application);
    }

    /**
     * Creates a PENDING offer for the supplied application.
     */
    private LoanOffer createPendingOffer(LoanApplication application, String lenderName) {
        LoanOffer offer = LoanOffer.builder()
                .loanApplication(application)
                .lenderName(lenderName)
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(500))
                .totalRepaymentAmount(BigDecimal.valueOf(60_000))
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        return loanOfferRepository.saveAndFlush(offer);
    }
}