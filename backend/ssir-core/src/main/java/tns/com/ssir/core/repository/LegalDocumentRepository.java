package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tns.com.ssir.core.entity.LegalDocument;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {
}