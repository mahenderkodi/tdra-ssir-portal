package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.CompanyAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyAddressRepository extends JpaRepository<CompanyAddress, Long> {
	
}