package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByTrackingId(String trackingId);
}