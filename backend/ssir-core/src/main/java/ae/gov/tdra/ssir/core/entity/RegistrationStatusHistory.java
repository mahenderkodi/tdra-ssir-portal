package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"registrationRequest", "changedBy"})
public class RegistrationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_reg"))
    private RegistrationRequest registrationRequest;

    @Column(name = "from_status", nullable = false, length = 30)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 30)
    private String toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", foreignKey = @ForeignKey(name = "fk_history_user"))
    private User changedBy; // User who updated status

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}