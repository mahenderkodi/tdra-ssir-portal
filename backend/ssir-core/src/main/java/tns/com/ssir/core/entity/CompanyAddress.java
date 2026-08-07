package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "company_addresses")
@Getter 
@Setter 
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
    @JsonIgnore 
    private Company company;

    @Column(name = "address_line_1", nullable = true) // Set to nullable for drafts
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "country", length = 100)
    private String country; 

    @Column(name = "emirate", length = 50)
    private String emirate; 

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}