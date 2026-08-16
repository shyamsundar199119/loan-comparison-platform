package eu.lendo.loancomparisonplatform.controller;

import eu.lendo.loancomparisonplatform.domain.OfferStatus;
import eu.lendo.loancomparisonplatform.dto.response.LoanOfferResponse;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationStateException;
import eu.lendo.loancomparisonplatform.exception.LoanOfferNotFoundException;
import eu.lendo.loancomparisonplatform.exception.LoanOfferStateException;
import eu.lendo.loancomparisonplatform.service.LoanOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@WebMvcTest(LoanOfferController.class)
class LoanOfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanOfferService loanOfferService;

    private UUID applicationId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        offerId = UUID.randomUUID();
    }

    @Test
    void acceptLoanOffer_whenServiceAcceptsOffer_returnsOk() throws Exception {

        LoanOfferResponse response = new LoanOfferResponse(
                offerId,
                applicationId,
                "Test Lender",
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(300000),
                Instant.now(),
                OfferStatus.ACCEPTED
        );

        when(loanOfferService.acceptLoanOffer(applicationId, offerId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, offerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerId").value(offerId.toString()))
                .andExpect(jsonPath("$.loanApplicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.lenderName").value("Test Lender"))
                .andExpect(jsonPath("$.annualInterestRate").value(5.5))
                .andExpect(jsonPath("$.monthlyPayment").value(2500))
                .andExpect(jsonPath("$.totalRepayment").value(300000))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(loanOfferService).acceptLoanOffer(applicationId, offerId);
    }

    @Test
    void acceptLoanOffer_whenApplicationDoesNotExist_returnsNotFound() throws Exception {
        when(loanOfferService.acceptLoanOffer(applicationId, offerId)).thenThrow(new LoanApplicationNotFoundException("Loan application not found: " +applicationId));
        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, offerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Loan application not found: " + applicationId));

        verify(loanOfferService).acceptLoanOffer(applicationId, offerId);
    }

    @Test
    void acceptLoanOffer_whenOfferDoesNotExist_returnsNotFound() throws Exception {
        when(loanOfferService.acceptLoanOffer(applicationId, offerId)).thenThrow(new LoanOfferNotFoundException("Loan offer not found: " + offerId));
        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, offerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Loan offer not found: " + offerId));

        verify(loanOfferService).acceptLoanOffer(applicationId, offerId);
    }

    @Test
    void acceptLoanOffer_whenApplicationIsNotPending_returnsConflict() throws Exception {

        when(loanOfferService.acceptLoanOffer(applicationId, offerId)).thenThrow(new LoanApplicationStateException("Loan application is not in PENDING status"));
        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, offerId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Loan application is not in PENDING status"));

        verify(loanOfferService).acceptLoanOffer(applicationId, offerId);
    }

    @Test
    void acceptLoanOffer_whenOfferIsNotPending_returnsConflict() throws Exception {

        when(loanOfferService.acceptLoanOffer(applicationId, offerId)).thenThrow(new LoanOfferStateException("Loan offer is not in PENDING status"));
        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, offerId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Loan offer is not in PENDING status"));
        verify(loanOfferService).acceptLoanOffer(applicationId, offerId);
    }

    @Test
    void acceptLoanOffer_whenApplicationIdIsInvalid_returnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", "invalid-uuid", offerId))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(loanOfferService);
    }

    @Test
    void acceptLoanOffer_whenOfferIdIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/application/{applicationId}/offers/{offerId}/accept", applicationId, "invalid-uuid"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(loanOfferService);
    }

}
