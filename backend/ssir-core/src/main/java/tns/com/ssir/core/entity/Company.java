package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_co_license", columnList = "trade_license_number")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"address", "contacts"})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", unique = true, length = 30)
    private String companyId; 

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "legal_entity_name", nullable = false)
    private String legalEntityName;

    @Column(name = "trade_license_number", unique = true, nullable = false, length = 50)
    private String tradeLicenseNumber;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "tax_vat_number", length = 50)
    private String taxVatNumber;

    @Column(name = "company_type", nullable = false, length = 50)
    private String companyType; 

    @Column(name = "industry_type", length = 100)
    private String industryType;

    @Column(name = "date_of_incorporation", nullable = false, length = 50)
    private String dateOfIncorporation; 

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "company_phone", nullable = false, length = 50)
    private String companyPhone;

    @Column(name = "website")
    private String website;

    @Column(name = "status", nullable = false, length = 30)
    private String status;
    
    @JsonIgnoreProperties("company")
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private CompanyAddress address;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("company")
    private List<CompanyContact> contacts = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}