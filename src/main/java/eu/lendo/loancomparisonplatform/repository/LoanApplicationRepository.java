package eu.lendo.loancomparisonplatform.repository;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    @Query("SELECT la FROM LoanApplication la " +
            "WHERE (:status IS NULL OR la.status = :status) " +
            "AND (CAST(:from AS timestamp) IS NULL OR la.createdAt >= :from) " +
            "AND (CAST(:to AS timestamp) IS NULL OR la.createdAt <= :to)")
    List<LoanApplication> findAllByFilters(@Param("status") ApplicationStatus status,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);

    List<LoanApplication> findAllByStatusAndExpiresAtBefore(ApplicationStatus status, Instant now);
}
