package eu.lendo.loancomparisonplatform.validation;

import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationRequestValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    // -------------------------------------------------------------------------
    // Valid request
    // -------------------------------------------------------------------------

    @Test
    void shouldPassValidationForValidRequest() {
        LoanApplicationRequest request = validRequest();
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    // -------------------------------------------------------------------------
    // First name validation
    // -------------------------------------------------------------------------

    @Test
    void shouldFailWhenFirstNameIsBlank() {

        LoanApplicationRequest request = new LoanApplicationRequest(
                "",
                "Sundar",
                "shyam@example.com",
                BigDecimal.valueOf(50000),
                12
        );

        assertViolation(request, "firstName", "First name is required");
    }

    @Test
    void shouldFailWhenFirstNameIsNull() {

        LoanApplicationRequest request = new LoanApplicationRequest(
                null,
                "Sundar",
                "shyam@example.com",
                BigDecimal.valueOf(50000),
                12
        );

        assertViolation(request, "firstName", "First name is required"
        );
    }

    @Test
    void shouldFailWhenFirstNameExceedsMaximumLength() {

        LoanApplicationRequest request = new LoanApplicationRequest(
                "a".repeat(101),
                "Sundar",
                "shyam@example.com",
                BigDecimal.valueOf(50000),
                12
        );

        assertViolation(request, "firstName", "First name must not exceed 100 characters");
    }

    // -------------------------------------------------------------------------
    // Last name validation
    // -------------------------------------------------------------------------

    @Test
    void shouldFailWhenLastNameIsBlank() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "",
                "shyam@example.com", BigDecimal.valueOf(50000), 12);
        assertViolation(request, "lastName", "Last name is required");
    }

    @Test
    void shouldFailWhenLastNameIsNull() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", null,
                "shyam@example.com", BigDecimal.valueOf(50000), 12);
        assertViolation(request, "lastName", "Last name is required");
    }

    @Test
    void shouldFailWhenLastNameExceedsMaximumLength() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "a".repeat(101),
                "shyam@example.com", BigDecimal.valueOf(50000), 12);
        assertViolation(request, "lastName", "Last name must not exceed 100 characters"
        );
    }

    // -------------------------------------------------------------------------
    // Email validation
    // -------------------------------------------------------------------------

    @Test
    void shouldFailWhenEmailIsBlank() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar",
                "", BigDecimal.valueOf(50000), 12);
        assertViolation(request, "emailId", "Email is required");
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar",
                null, BigDecimal.valueOf(50000), 12);
        assertViolation(request, "emailId", "Email is required");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "invalid-email",
                BigDecimal.valueOf(50000), 12);
        assertViolation(request, "emailId", "Email must be valid");
    }

    @Test
    void shouldFailWhenEmailExceedsMaximumLength() {
        String email = "a".repeat(250) + "@test.com";
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", email,
                BigDecimal.valueOf(50000), 12);
        assertViolation(request, "emailId", "Email must not exceed 255 characters");
    }

    // -------------------------------------------------------------------------
    // Request amount validation
    // -------------------------------------------------------------------------

    @Test
    void shouldFailWhenRequestAmountIsNull() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar",
                "shyam@example.com", null, 12);
        assertViolation(request, "requestAmount", "Request amount is required");
    }

    @Test
    void shouldFailWhenRequestAmountIsBelowMinimum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar",
                "shyam@example.com", BigDecimal.valueOf(999), 12);
        assertViolation(request, "requestAmount", "Minimum requested loan amount must be greater than or equal to 1,000");
    }

    @Test
    void shouldPassWhenRequestAmountIsExactlyMinimum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(1000), 12);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldPassWhenRequestAmountIsExactlyMaximum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(1_000_000), 12);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldFailWhenRequestAmountIsAboveMaximum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(1_000_001), 12);
        assertViolation(request, "requestAmount", "Maximum requested loan amount must be less than or equal to 1,000,000");
    }

    // -------------------------------------------------------------------------
    // Loan term validation
    // -------------------------------------------------------------------------

    @Test
    void shouldFailWhenLoanTermIsNull() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), null);
        assertViolation(request, "loanTermMonths", "Loan term is required");
    }

    @Test
    void shouldFailWhenLoanTermIsBelowMinimum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), 2);
        assertViolation(request, "loanTermMonths", "Minimum loan term must be greater than or equal to 3 months");
    }

    @Test
    void shouldPassWhenLoanTermIsExactlyMinimum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), 3);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldPassWhenLoanTermIsExactlyMaximum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), 360);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldFailWhenLoanTermIsAboveMaximum() {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), 361);
        assertViolation(request, "loanTermMonths", "Maximum loan term must be less than or equal to 360 months(30 years)");
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private LoanApplicationRequest validRequest() {
        return new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com",
                BigDecimal.valueOf(50000), 12);
    }

    private void assertViolation(LoanApplicationRequest request, String property, String expectedMessage) {
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals(property)
                                && violation.getMessage().equals(expectedMessage));
    }
}
