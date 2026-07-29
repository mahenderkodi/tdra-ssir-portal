package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.CompanyAddress;

public interface CompanyAddressRepository extends JpaRepository<CompanyAddress, Long> {
	
}