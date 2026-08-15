package eu.lendo.loancomparisonplatform.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        @NotBlank(message = "First name is required")
        @Size(max=100, message = "First name must not exceed 100 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(max=100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message="Email must be valid")
        @Size(max=255, message = "Email must not exceed 255 characters")
        String emailId,

        @NotNull(message = "Request amount is required")
        @DecimalMin(value = "1000", message = "Minimum requested loan amount must be greater than or equal to 1,000")
        @DecimalMax(value = "1000000", message = "Maximum requested loan amount must be less than or equal to 1,000,000")
        BigDecimal requestAmount,

        @NotNull(message = "Loan term is required")
        @Min(value = 3, message = "Minimum loan term must be greater than or equal to 3 months")
        @Max(value = 360, message = "Maximum loan term must be less than or equal to 360 months(30 years)")
        Integer loanTermMonths
) {
}
