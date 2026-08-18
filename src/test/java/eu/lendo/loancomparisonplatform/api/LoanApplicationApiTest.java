package eu.lendo.loancomparisonplatform.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class LoanApplicationApiTest extends BaseApiTest {

    @Test
    void createApplication_withValidRequest_returnsCreatedApplication() {

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(validLoanApplicationPayload())
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(201)
                .body("applicationId", notNullValue())
                .body("applicantFirstName", equalTo("Shyam Sundar"))
                .body("applicantLastName", equalTo("Durai Pandian"))
                .body("applicantEmail", equalTo("shyam@example.com"))
                .body("loanAmount", comparesEqualTo(50000))
                .body("loanTermMonths", equalTo(12))
                .body("status", equalTo("PENDING"))
                .body("createdAt", notNullValue())
                .body("expiresAt", notNullValue())
                .body("loanOffers", empty());
    }

    @Test
    void getApplication_afterCreation_returnsApplication() {

        String applicationId =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(validLoanApplicationPayload())
                        .when()
                        .post("/api/v1/application")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("applicationId");

        RestAssured.given()
                .pathParam("applicationId", applicationId)
                .when()
                .get("/api/v1/application/{applicationId}")
                .then()
                .statusCode(200)
                .body("applicationId", equalTo(applicationId))
                .body("applicantFirstName", equalTo("Shyam Sundar"))
                .body("applicantLastName", equalTo("Durai Pandian"))
                .body("applicantEmail", equalTo("shyam@example.com"))
                .body("loanAmount", comparesEqualTo(50000.0F))
                .body("loanTermMonths", equalTo(12))
                .body("status", equalTo("PENDING"))
                .body("createdAt", notNullValue())
                .body("expiresAt", notNullValue())
                .body("loanOffers", empty());
    }

    @Test
    void getApplication_withNonExistingId_returnsNotFound() {

        UUID applicationId = UUID.randomUUID();

        RestAssured.given()
                .pathParam("applicationId", applicationId)
                .when()
                .get("/api/v1/application/{applicationId}")
                .then()
                .statusCode(404);
    }

    @Test
    void getApplication_withInvalidUuid_returnsBadRequest() {

        RestAssured.given()
                .when()
                .get("/api/v1/application/invalid-uuid")
                .then()
                .statusCode(400);
    }

    @Test
    void listApplications_withoutFilters_returnsApplications() {

        createPendingApplication();

        RestAssured.given()
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Ensure the list is not empty
                .body("[0].applicationId", notNullValue()) // Check the first item has an applicationId
                .body("[0].status", equalTo("PENDING"));
    }

    @Test
    void listApplications_withStatusFilter_returnsApplications() {

        RestAssured.given()
                .queryParam("status", "PENDING")
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].applicationId", notNullValue())
                .body("[0].status", equalTo("PENDING"));
    }

    @Test
    void listApplications_withDateRange_returnsApplications() {

        createPendingApplication(); // Ensure at least one application exists in the date range

        RestAssured.given()
                .queryParam("from", "2026-08-01T00:00:00Z")
                .queryParam("to", Instant.now().plusSeconds(3600).toString()) // Future date to ensure the application is included
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].applicationId", notNullValue())
                .body("[0].status", equalTo("PENDING"));
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

    @Test
    void listApplications_withAllFilters_returnsApplications() {

        RestAssured.given()
                .queryParam("status", "PENDING")
                .queryParam("from", "2026-08-01T00:00:00Z")
                .queryParam("to", "2026-08-31T23:59:59Z")
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].applicationId", notNullValue())
                .body("[0].status", equalTo("PENDING"));
    }

    @Test
    void createApplication_withAmountBelowMinimum_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.put("requestAmount", 999);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void createApplication_withAmountAboveMaximum_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.put("requestAmount", 1_000_001);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void createApplication_withLoanTermBelowMinimum_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.put("loanTermMonths", 2);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void createApplication_withLoanTermAboveMaximum_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.put("loanTermMonths", 361);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void createApplication_withInvalidEmail_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.put("emailId", "invalid-email");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void createApplication_withMissingRequiredField_returnsBadRequest() {

        Map<String, Object> payload =
                new HashMap<>(validLoanApplicationPayload());

        payload.remove("emailId");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Validation failed"));
    }

    @Test
    void listApplications_withInvalidStatus_returnsBadRequest() {

        RestAssured.given()
                .queryParam("status", "INVALID_STATUS")
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Invalid value for parameter 'status'"));
    }

    @Test
    void listApplications_withInvalidFromDate_returnsBadRequest() {

        RestAssured.given()
                .queryParam("from", "not-a-date")
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("Invalid value for parameter 'from'"));
    }

    @Test
    void listApplications_whenFromIsAfterTo_returnsBadRequest() {

        RestAssured.given()
                .queryParam("from", "2026-08-20T00:00:00Z")
                .queryParam("to", "2026-08-10T00:00:00Z")
                .when()
                .get("/api/v1/application")
                .then()
                .statusCode(400).body("message", containsString("'from' must not be after 'to'"));
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
}