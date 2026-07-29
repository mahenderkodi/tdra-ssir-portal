package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.CompanyContact;

public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {
	
}