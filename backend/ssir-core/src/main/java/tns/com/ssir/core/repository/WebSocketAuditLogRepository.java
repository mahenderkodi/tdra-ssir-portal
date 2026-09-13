package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tns.com.ssir.core.entity.WebSocketAuditLog;

@Repository
public interface WebSocketAuditLogRepository extends JpaRepository<WebSocketAuditLog, Long> {
}