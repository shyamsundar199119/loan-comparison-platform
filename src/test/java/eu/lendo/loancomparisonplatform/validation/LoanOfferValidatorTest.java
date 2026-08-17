package eu.lendo.loancomparisonplatform.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import eu.lendo.loancomparisonplatform.dto.request.LoanOfferRequest;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanOfferValidatorTest {

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

    private static LoanOfferRequest validRequest() {
        return new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
    }

    @Test
    void validLoanOfferRequest_hasNoValidationErrors() {
        LoanOfferRequest request = validRequest();
        Set<ConstraintViolation<LoanOfferRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void lenderName_whenNull_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest(null, BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "lenderName");
    }

    @Test
    void lenderName_whenBlank_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("   ", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "lenderName");
    }

    @Test
    void lenderName_whenExceedsMaximumLength_hasValidationError() {
        String lenderName = "A".repeat(101);
        LoanOfferRequest request = new LoanOfferRequest(lenderName, BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "lenderName");
    }

    @Test
    void annualInterestRate_whenNull_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", null, BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "annualInterestRate");
    }

    @Test
    void annualInterestRate_whenZero_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.ZERO, BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "annualInterestRate");
    }

    @Test
    void annualInterestRate_whenNegative_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(-1), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "annualInterestRate");
    }

    @Test
    void annualInterestRate_whenMoreThanTwoDecimalPlaces_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", new BigDecimal("5.555"), BigDecimal.valueOf(2500), BigDecimal.valueOf(300000));
        assertViolation(request, "annualInterestRate");
    }

    @Test
    void monthlyPaymentAmount_whenNull_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), null, BigDecimal.valueOf(300000));
        assertViolation(request, "monthlyPaymentAmount");
    }

    @Test
    void monthlyPaymentAmount_whenZero_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.ZERO, BigDecimal.valueOf(300000));
        assertViolation(request, "monthlyPaymentAmount");
    }

    @Test
    void monthlyPaymentAmount_whenNegative_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(-2500), BigDecimal.valueOf(300000));
        assertViolation(request, "monthlyPaymentAmount");
    }

    @Test
    void monthlyPaymentAmount_whenMoreThanTwoDecimalPlaces_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), new BigDecimal("2500.123"), BigDecimal.valueOf(300000));
        assertViolation(request, "monthlyPaymentAmount");
    }

    @Test
    void totalRepaymentAmount_whenNull_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), null);
        assertViolation(request, "totalRepaymentAmount");
    }

    @Test
    void totalRepaymentAmount_whenZero_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.ZERO);
        assertViolation(request, "totalRepaymentAmount");
    }

    @Test
    void totalRepaymentAmount_whenNegative_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), BigDecimal.valueOf(-300000));
        assertViolation(request, "totalRepaymentAmount");
    }

    @Test
    void totalRepaymentAmount_whenMoreThanTwoDecimalPlaces_hasValidationError() {
        LoanOfferRequest request = new LoanOfferRequest("Test Lender", BigDecimal.valueOf(5.5), BigDecimal.valueOf(2500), new BigDecimal("300000.123"));
        assertViolation(request, "totalRepaymentAmount");
    }

    private void assertViolation(LoanOfferRequest request, String property) {
        Set<ConstraintViolation<LoanOfferRequest>> violations = validator.validate(request);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).contains(property);
    }
}
