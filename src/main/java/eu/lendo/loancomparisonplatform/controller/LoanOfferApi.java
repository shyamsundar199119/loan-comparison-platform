package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.dto.request.LoanOfferRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(
        name = "Loan Offers",
        description = "APIs for submitting and accepting lender loan offers"
)
public interface LoanOfferApi {

    @Operation(
            summary = "Accept a loan offer",
            description = """
                    Accepts a lender's offer for a loan application.
                    The application must be in PENDING status and the offer
                    must be eligible for acceptance.

                    Once an offer is accepted, the application becomes ACCEPTED
                    and all other offers associated with the application are rejected.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan offer accepted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan application or loan offer not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Application or offer is not in a valid state for acceptance"
            )
    })
    ResponseEntity<LoanOfferResponse> acceptLoanOffer(UUID applicationId, UUID offerId);

    @Operation(
            summary = "Submit a loan offer",
            description = """
                    Allows a lender to submit a loan offer for a pending
                    loan application.

                    Each lender can submit at most one offer for an application.
                    Offers cannot be submitted once the application is no longer
                    in PENDING status.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Loan offer created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanOfferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan offer request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan application not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = """
                            Application is not PENDING or the lender has
                            already submitted an offer for this application
                            """
            )
    })
    ResponseEntity<LoanOfferResponse> saveLoanOffer(UUID applicationId, LoanOfferRequest request);
}
