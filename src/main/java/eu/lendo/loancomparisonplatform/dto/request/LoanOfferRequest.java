package eu.lendo.loancomparisonplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoanOfferRequest(

        @NotBlank(message = "Lender name must not be blank")
        @Size(max = 100, message = "Lender name must not exceed 100 characters")
        String lenderName,

        @NotNull(message = "Annual interest rate is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Annual interest rate must be greater than 0")
        @Digits(integer = 5, fraction = 2, message = "Annual interest rate must have at most 5 integer digits and 2 decimal places")
        BigDecimal annualInterestRate,

        @NotNull(message = "Monthly payment amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Monthly payment amount must be greater than 0")
        @Digits(integer = 12, fraction = 2, message = "Monthly payment amount must have at most 12 integer digits and 2 decimal places")
        BigDecimal monthlyPaymentAmount,

        @NotNull(message = "Total repayment amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Total repayment amount must be greater than 0")
        @Digits(integer = 14, fraction = 2, message = "Total repayment amount must have at most 14 integer digits and 2 decimal places")
        BigDecimal totalRepaymentAmount) {
}
