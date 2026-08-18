package eu.lendo.loancomparisonplatform.repository;

import eu.lendo.loancomparisonplatform.domain.ApplicationStatus;
import eu.lendo.loancomparisonplatform.domain.LoanApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    @Query("SELECT la FROM LoanApplication la " +
            "WHERE (:status IS NULL OR la.status = :status) " +
            "AND (CAST(:from AS timestamp) IS NULL OR la.createdAt >= :from) " +
            "AND (CAST(:to AS timestamp) IS NULL OR la.createdAt <= :to)")
    List<LoanApplication> findAllByFilters(@Param("status") ApplicationStatus status,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT la FROM LoanApplication la WHERE la.id = :loanApplicationId")
    Optional<LoanApplication> findByIdWithLock(@Param("loanApplicationId") UUID loanApplicationId);

    List<LoanApplication> findAllByStatusAndExpiresAtBefore(ApplicationStatus status, Instant now);
}
