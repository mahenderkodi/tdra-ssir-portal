package tns.com.ssir.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tns.com.ssir.core.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}