package tns.com.ssir.core.repository;
  
import org.springframework.data.jpa.repository.JpaRepository;
import tns.com.ssir.core.entity.AuditLog;
 
  public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  
  }
 


