package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "company")
public class CompanyAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_company"))
    private Company company;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "emirate", nullable = false, length = 50)
    private String emirate; // Dubai, Abu Dhabi, etc.

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}