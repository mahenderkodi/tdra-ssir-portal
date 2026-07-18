package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	
}