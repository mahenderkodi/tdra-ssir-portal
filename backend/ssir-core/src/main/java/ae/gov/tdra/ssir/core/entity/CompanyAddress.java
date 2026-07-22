package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import Jackson Annotation

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
    @JsonIgnore // Add this annotation back to prevent infinite loop
    private Company company;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

   // @Column(name = "address_line_2")
   // private String addressLine2;

    @Column(name = "country", nullable = false, length = 100)
    private String country; 

    @Column(name = "emirate", nullable = false, length = 50)
    private String emirate; 

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}