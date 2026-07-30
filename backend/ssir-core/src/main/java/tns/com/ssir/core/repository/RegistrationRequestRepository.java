package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.RegistrationRequest;

import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByTrackingId(String trackingId);
    
    Optional<RegistrationRequest> findByCompany(Company company);
}