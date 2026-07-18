package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserIdString(String userIdString);
}