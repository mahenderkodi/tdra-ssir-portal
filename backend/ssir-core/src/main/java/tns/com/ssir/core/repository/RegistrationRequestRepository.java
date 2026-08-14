package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.RegistrationRequest;

import java.util.List; // Added import [3]
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByTrackingId(String trackingId);
    
    // FIX: Changed return type from Optional to List to support multiple requests [3]
    List<RegistrationRequest> findByCompany(Company company);
}