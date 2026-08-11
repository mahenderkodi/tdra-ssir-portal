package tns.com.ssir.core.repository;

import tns.com.ssir.core.entity.SenderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SenderIdRepository extends JpaRepository<SenderId, Long> {
    
    // Fetch all active Sender IDs belonging to a specific company ID [3]
    List<SenderId> findByCompanyId(Long companyId);

    Optional<SenderId> findBySenderIdName(String senderIdName);
    
    long countByCompanyIdAndStatus(Long companyId, String status);

    // Queries to calculate dashboard metrics cleanly [3]
    long countByCompanyIdAndStatusIn(Long companyId, List<String> statuses);
    
    long countByCompanyId(Long companyId);
    
    Optional<SenderId> findByTrackingId(String trackingId);
}