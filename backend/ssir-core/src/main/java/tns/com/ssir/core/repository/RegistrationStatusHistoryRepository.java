package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.RegistrationStatusHistory;

public interface RegistrationStatusHistoryRepository extends JpaRepository<RegistrationStatusHistory, Long> {
	
}