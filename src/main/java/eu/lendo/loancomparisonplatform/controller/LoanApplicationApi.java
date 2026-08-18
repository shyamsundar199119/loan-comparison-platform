package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Tag(
        name = "Loan Applications",
        description = "APIs for creating, retrieving and listing loan applications"
)
public interface LoanApplicationApi {
    @Operation(
            summary = "Create a loan application",
            description = """
                    Creates a new loan application for a customer.
                    A newly created application starts with PENDING status.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Loan application created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanApplicationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan application request"
            )
    })
    ResponseEntity<LoanApplicationResponse> createApplication(LoanApplicationRequest request);

    @Operation(
            summary = "Get a loan application",
            description = """
                    Retrieves a single loan application by its ID,
                    including all loan offers associated with the application.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan application retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanApplicationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan application not found"
            )
    })
    ResponseEntity<LoanApplicationResponse> getApplication(UUID applicationId);

    @Operation(
            summary = "List loan applications",
            description = """
                    Retrieves loan applications with optional filtering by
                    application status and creation date range.
                    If no filters are provided, all applications are returned.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range or request parameters"
            )
    })
    ResponseEntity<List<LoanApplicationResponse>> listApplications(ApplicationStatus status,Instant from,Instant to);
}
