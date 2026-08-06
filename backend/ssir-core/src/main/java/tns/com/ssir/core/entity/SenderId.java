package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sender_ids", indexes = {
    @Index(name = "idx_sender_name", columnList = "sender_id_name"),
    @Index(name = "idx_sender_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "company")
public class SenderId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id_name", nullable = false, length = 11)
    private String senderIdName; // e.g., "TDRA-SSIR"

    @Column(name = "status", nullable = false, length = 30)
    private String status; // ACTIVE, PENDING, EXPIRED, SUSPENDED, REVOKED [3]

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate; // Managed by background schedulers [3]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sender_company"))
    private Company company;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING"; // Default initial state
        }
        if (this.expirationDate == null) {
            this.expirationDate = LocalDate.now().plusYears(1); // Default expires in 1 year [3]
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}