package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.RegistrationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationStatusHistoryRepository extends JpaRepository<RegistrationStatusHistory, Long> {
	
}