package ae.gov.tdra.ssir.core.repository;

import ae.gov.tdra.ssir.core.entity.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {
}