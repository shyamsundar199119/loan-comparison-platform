package eu.lendo.loancomparisonplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.dto.request.LoanApplicationRequest;
import eu.lendo.loancomparisonplatform.dto.response.LoanApplicationResponse;
import eu.lendo.loancomparisonplatform.exception.LoanApplicationNotFoundException;
import eu.lendo.loancomparisonplatform.service.LoanApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanApplicationService loanApplicationService;

    @Test
    void createApplication_validRequest_return201WithBody() throws Exception {
        LoanApplicationRequest request = new LoanApplicationRequest("Shyam", "Sundar", "shyam@example.com", BigDecimal.valueOf(50000), 12);
        UUID applicationId = UUID.randomUUID();
        LoanApplicationResponse response = new LoanApplicationResponse(applicationId, "Shyam", "Sundar", "shyam@example.com", BigDecimal.valueOf(50000), 12, Instant.now().plusSeconds(86400), Instant.now(), ApplicationStatus.PENDING, List.of());

        when(loanApplicationService.createLoanApplication(any(LoanApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.applicantFirstName").value("Shyam"))
                .andExpect(jsonPath("$.applicantLastName").value("Sundar"))
                .andExpect(jsonPath("$.applicantEmail").value("shyam@example.com"))
                .andExpect(jsonPath("$.loanAmount").value(BigDecimal.valueOf(50000)))
                .andExpect(jsonPath("$.loanTermMonths").value(12))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.loanOffers").isArray())
                .andExpect(jsonPath("$.loanOffers").isEmpty());

        verify(loanApplicationService).createLoanApplication(any(LoanApplicationRequest.class));
    }

    @Test
    void createApplication_invalidRequest_return400() throws Exception {
        String requestBody = """
                {
                    "firstName": "",
                    "lastName": "Sundar",
                    "emailId": "shyam@example.com",
                    "requestAmount": 50000,
                    "loanTermMonths": 12
                }
                """;

        mockMvc.perform(post("/api/v1/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.firstName").value("First name is required"));

        verify(loanApplicationService, never()).createLoanApplication(any(LoanApplicationRequest.class));
    }

    @Test
    void getApplication_existingId_return200WithAllFields() throws Exception {
        UUID applicationId = UUID.randomUUID();
        LoanApplicationResponse response = new LoanApplicationResponse(applicationId, "Shyam", "Sundar",
                "shyam@example.com", BigDecimal.valueOf(50000), 12, Instant.now().plusSeconds(86400), Instant.now(), ApplicationStatus.PENDING, List.of());

        when(loanApplicationService.getLoanApplication(applicationId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/application/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.applicantFirstName").value("Shyam"))
                .andExpect(jsonPath("$.applicantLastName").value("Sundar"))
                .andExpect(jsonPath("$.applicantEmail").value("shyam@example.com"))
                .andExpect(jsonPath("$.loanAmount").value(BigDecimal.valueOf(50000)))
                .andExpect(jsonPath("$.loanTermMonths").value(12))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.loanOffers").isArray())
                .andExpect(jsonPath("$.loanOffers").isEmpty());


        verify(loanApplicationService).getLoanApplication(applicationId);
    }

    @Test
    void getApplication_invalidRequest_return400() throws Exception {
        mockMvc.perform(get("/api/v1/application/{applicationId}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'applicationId'"));;

        verify(loanApplicationService, never()).getLoanApplication(any(UUID.class));
    }

    @Test
    void getApplication_noloanApplicationFound_return404() throws Exception {
        UUID applicationId = UUID.randomUUID();

        when(loanApplicationService.getLoanApplication(applicationId))
                .thenThrow(new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        mockMvc.perform(get("/api/v1/application/{applicationId}", applicationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Loan application not found: " + applicationId))
                .andExpect(jsonPath("$.path").value("/api/v1/application/" + applicationId))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());

        verify(loanApplicationService).getLoanApplication(applicationId);
    }

    @Test
    void listApplication_noFilter_return200() throws Exception {
        when(loanApplicationService.listLoanApplications(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/application"))
                .andExpect(status().isOk());

        verify(loanApplicationService).listLoanApplications(null, null, null);
    }

    @Test
    void listApplication_statusFilter_return200() throws Exception {
        when(loanApplicationService.listLoanApplications(ApplicationStatus.PENDING, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/application")
                        .param("status", "PENDING"))
                .andExpect(status().isOk());

        verify(loanApplicationService).listLoanApplications(ApplicationStatus.PENDING, null, null);
    }

    @Test
    void listApplication_dateFilter_return200() throws Exception {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-15T00:00:00Z");

        when(loanApplicationService.listLoanApplications(null, from, to)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/application")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk());

        verify(loanApplicationService).listLoanApplications(null, from, to);
    }

    @Test
    void listApplication_invalidStatus_return400() throws Exception {
        mockMvc.perform(get("/api/v1/application")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'status'"));


        verify(loanApplicationService, never()).listLoanApplications(any(), any(), any());
    }

    @Test
    void listApplication_invalidFromDate_return400() throws Exception {
        mockMvc.perform(get("/api/v1/application")
                        .param("from", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'from'"));

        verify(loanApplicationService, never()).listLoanApplications(any(), any(), any());
    }

    @Test
    void listApplication_incorrectRequest_return400() throws Exception {
        Instant from = Instant.parse("2026-08-20T00:00:00Z");
        Instant to = Instant.parse("2026-08-10T00:00:00Z");

        mockMvc.perform(get("/api/v1/application")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isBadRequest());

        verify(loanApplicationService, never()).listLoanApplications(any(), any(), any());
    }
}