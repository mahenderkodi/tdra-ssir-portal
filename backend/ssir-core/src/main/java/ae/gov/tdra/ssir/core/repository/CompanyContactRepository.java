package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {
	
}