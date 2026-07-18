package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName; // e.g., ROLE_TDRA_SUPER_ADMIN, ROLE_COMPANY_ADMIN, ROLE_COMPANY_USER
}