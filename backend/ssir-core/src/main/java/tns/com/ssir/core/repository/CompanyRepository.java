package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tns.com.ssir.core.entity.Company;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
  
    Optional<Company> findByTradeLicenseNumber(String tradeLicenseNumber);
    
}