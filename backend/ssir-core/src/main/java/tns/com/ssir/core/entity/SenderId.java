package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sender_ids")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sender_id_name", nullable = false, unique = true)
    private String senderIdName;

    // Added to support frontend tracking-id requirement [3]
    @Column(name = "tracking_id", unique = true)
    private String trackingId;

    // Added to support detail justification
    @Column(name = "justification", length = 1000)
    private String justification;

    // Added to support admin feedback comments/remarks [3]
    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "status", nullable = false)
    private String status;

    // Added to store the MinIO object key reference [2]
    @Column(name = "auth_letter_path")
    private String authLetterPath;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}