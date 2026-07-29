package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserIdString(String userIdString);
    Optional<User> findByCompany(Company company);
}