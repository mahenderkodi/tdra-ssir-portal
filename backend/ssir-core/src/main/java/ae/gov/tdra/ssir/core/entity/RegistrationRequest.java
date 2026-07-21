package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "registration_requests", indexes = {
    @Index(name = "idx_reg_tracking", columnList = "tracking_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"company", "reviewer"})
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, length = 50)
    private String trackingId; // e.g., REG-2026-00001

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_reg_company"))
    private Company company;

    @Column(name = "current_status", nullable = false, length = 30)
    private String currentStatus; // DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, INFO_REQUESTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", foreignKey = @ForeignKey(name = "fk_reg_reviewer"))
    private User reviewer; // TDRA Administrator assigned to review

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "info_request_comments", columnDefinition = "TEXT")
    private String infoRequestComments;

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