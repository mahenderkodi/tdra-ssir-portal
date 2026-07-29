package tns.com.ssir;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import tns.com.ssir.core.entity.Role;
import tns.com.ssir.core.entity.User;
import tns.com.ssir.core.repository.RoleRepository;
import tns.com.ssir.core.repository.UserRepository;

@SpringBootApplication(scanBasePackages = "tns.com.ssir")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "tns.com.ssir.core.repository")
@EntityScan(basePackages = "tns.com.ssir.core.entity")
public class SSIRApplication {

    public static void main(String[] args) {
        SpringApplication.run(SSIRApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSecurityData(
            RoleRepository roleRepository, 
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // 1. List of all 9 enterprise roles defined across the 3 Security Domains
                List<String> expectedRoles = Arrays.asList(
                    // Group A: TDRA Regulator Domain (Internal Staff)
                    "ROLE_TDRA_SUPER_ADMIN",
                    "ROLE_TDRA_REVIEWER",
                    "ROLE_TDRA_APPROVER",
                    "ROLE_TDRA_AUDITOR",
                    
                    // Group B: Enterprise Client Domain (Multi-Tenant Corporate Users)
                    "ROLE_COMPANY_ADMIN",
                    "ROLE_COMPANY_USER",
                    "ROLE_COMPANY_VIEWER",
                    
                    // Group C: Telecom Operator Domain (Network Carriers)
                    "ROLE_MNO_ADMIN",
                    "ROLE_MNO_OPERATOR"
                );

                // 2. Seed all 9 roles safely (idempotent checks prevent duplicate key errors)
                for (String roleName : expectedRoles) {
                    if (roleRepository.findByRoleName(roleName).isEmpty()) {
                        roleRepository.save(Role.builder().roleName(roleName).build());
                        System.out.println(">>> SECURITY SEEDER: Registered role: " + roleName);
                    }
                }

                // 3. Fetch the Super Admin role specifically to assign it to our default test user
                Role superAdminRole = roleRepository.findByRoleName("ROLE_TDRA_SUPER_ADMIN")
                        .orElseThrow(() -> new IllegalStateException("Core role ROLE_TDRA_SUPER_ADMIN not found"));

                // 4. Seed 'tdra_admin' User if they do not exist
                if (userRepository.findByUsername("tdra_admin").isEmpty()) {
                    Set<Role> roles = new HashSet<>();
                    roles.add(superAdminRole);

                    User admin = User.builder()
                            .id(100L) // Fixed ID to keep consistent
                            .userIdString("USR888888")
                            .username("tdra_admin")
                            .email("admin@ssir.gov.ae")
                            // Encrypts "Password123!" dynamically using the active PasswordEncoder inside your JVM
                            .passwordHash(passwordEncoder.encode("Password123!"))
                            .status("ACTIVE")
                            .roles(roles)
                            .build();

                    userRepository.save(admin);
                    System.out.println(">>> SECURITY SEEDER: Successfully auto-created 'tdra_admin' with password 'Password123!'");
                } else {
                    System.out.println(">>> SECURITY SEEDER: 'tdra_admin' record already exists.");
                }
            } catch (Exception e) {
                System.err.println(">>> SECURITY SEEDER FAILURE: " + e.getMessage());
            }
        };
    }
}