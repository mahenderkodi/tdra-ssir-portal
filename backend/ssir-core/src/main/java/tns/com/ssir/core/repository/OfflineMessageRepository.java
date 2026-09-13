package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tns.com.ssir.core.entity.OfflineMessage;
import java.util.List;

@Repository
public interface OfflineMessageRepository extends JpaRepository<OfflineMessage, Long> {
    
    // Retrieves pending offline messages ordered chronologically [2]
    List<OfflineMessage> findByUsernameOrderByCreatedAtAsc(String username);
    
    // Purges queue once delivered safely [2]
    void deleteByUsername(String username);
}