package eu.lendo.loancomparisonplatform.api;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import eu.lendo.loancomparisonplatform.repository.LoanApplicationRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class LoanOfferApiTest extends BaseApiTest {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;


    @Test
    void createLoanOffer_whenApplicationIsPending_returnsCreatedOffer() {

        String applicationId = createPendingApplication();

        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(201)
                .body("offerId", notNullValue())
                .body("loanApplicationId", equalTo(applicationId))
                .body("lenderName", equalTo("Test Lender"))
                .body("annualInterestRate", comparesEqualTo(5.5F))
                .body("monthlyPayment", comparesEqualTo(2500))
                .body("totalRepayment", comparesEqualTo(300000))
                .body("status", equalTo("PENDING"))
                .body("createdAt", notNullValue());
    }

    @Test
    void createLoanOffer_whenApplicationDoesNotExist_returnsNotFound() {

        String applicationId = UUID.randomUUID().toString();

        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(404);
    }

    @Test
    void createLoanOffer_whenApplicationIsNotPending_returnsConflict() {

        String applicationId = createApplicationWithAcceptedOffer();

        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(422);
    }

    @Test
    void createLoanOffer_whenSameLenderAlreadySubmittedOffer_returnsConflict() {

        String applicationId = createPendingApplication();

        // First offer
        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(201);

        // Same lender submits another offer
        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(409);
    }

    @Test
    void createLoanOffer_whenDifferentLenderSubmitsOffer_returnsCreatedOffer() {

        String applicationId = createPendingApplication();

        // First lender
        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(201);

        // Different lender
        String secondLenderPayload = """
                {
                    "lenderName": "Another Lender",
                    "annualInterestRate": 6.0,
                    "monthlyPaymentAmount": 2600,
                    "totalRepaymentAmount": 312000
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(secondLenderPayload)
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(201)
                .body("loanApplicationId", equalTo(applicationId))
                .body("lenderName", equalTo("Another Lender"))
                .body("annualInterestRate", comparesEqualTo(6.0F))
                .body("monthlyPayment", comparesEqualTo(2600))
                .body("totalRepayment", comparesEqualTo(312000))
                .body("status", equalTo("PENDING"));
    }

    @Test
    void createLoanOffer_whenApplicationIsExpired_returnsConflict() {

        String applicationId = createExpiredApplication();

        given()
                .contentType(ContentType.JSON)
                .pathParam("applicationId", applicationId)
                .body(validLoanOfferPayload())
                .when()
                .post("/api/v1/application/{applicationId}/offers")
                .then()
                .statusCode(422);
    }

    private String createPendingApplication() {

        return given()
                .contentType(ContentType.JSON)
                .body(validLoanApplicationPayload())
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(201)
                .extract()
                .path("applicationId");
    }

    private Map<String, Object> validLoanOfferPayload() {

        return Map.of(
                "lenderName", "Test Lender",
                "annualInterestRate", 5.5,
                "monthlyPaymentAmount", 2500,
                "totalRepaymentAmount", 300000
        );
    }

    private Map<String, Object> validLoanApplicationPayload() {

        return Map.of(
                "firstName", "Shyam Sundar",
                "lastName", "Durai Pandian",
                "emailId", "shyam@example.com",
                "requestAmount", 50000,
                "loanTermMonths", 12
        );
    }

    private String createApplicationWithAcceptedOffer() {

        Instant now = Instant.now();

        LoanApplication application = LoanApplication.builder()
                .applicantFirstName("Shyam Sundar")
                .applicantLastName("Durai Pandian")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(50000))
                .requestLoanTermMonths(12)
                .status(ApplicationStatus.ACCEPTED)
                .createdAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .build();

        LoanApplication savedApplication =
                loanApplicationRepository.saveAndFlush(application);

        return savedApplication.getId().toString();
    }

    private String createExpiredApplication() {

        LoanApplication application = LoanApplication.builder()
                .applicantFirstName("Shyam")
                .applicantLastName("Sundar")
                .applicantEmail("shyam@example.com")
                .requestLoanAmount(BigDecimal.valueOf(50000))
                .requestLoanTermMonths(12)
                .status(ApplicationStatus.EXPIRED)
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        loanApplicationRepository.saveAndFlush(application);

        return application.getId().toString();
    }
}