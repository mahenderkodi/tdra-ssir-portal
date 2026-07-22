package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    // Custom database queries to verify duplicates during registration validation
    Optional<Company> findByTradeLicenseNumber(String tradeLicenseNumber);
    Optional<Company> findByCompanyId(String companyId);
}