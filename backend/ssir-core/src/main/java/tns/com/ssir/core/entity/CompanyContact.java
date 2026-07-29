package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "company_contacts")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "company")
public class CompanyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contact_company"))
    @JsonIgnore
    private Company company;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "designation", nullable = false, length = 50)
    private String designation;

    @Column(name = "department", length = 100)
    private String department; 

    @Column(name = "official_email", nullable = false, length = 100)
    private String officialEmail;

    @Column(name = "mobile_number", nullable = false, length = 30)
    private String mobileNumber;

    @Column(name = "office_number", length = 30)
    private String officeNumber; 

    @Column(name = "address", length = 500)
    private String address; 

    @Column(name = "uae_pass_id", length = 100)
    private String uaePassId;

    @Column(name = "passport_emirates_id", length = 50)
    private String passportEmiratesId;
}