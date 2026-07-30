package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_uae_pass", columnList = "uae_pass_uuid")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"roles", "company"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_string", unique = true, nullable = false, length = 30)
    private String userIdString; 

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "uae_pass_uuid", unique = true, length = 100)
    private String uaePassUuid;

    @Column(name = "status", nullable = false, length = 30)
    private String status; 

    // --- NEW ACCOUNT PREFERENCE FIELDS ---
    @Column(name = "preferred_language", nullable = false, length = 10)
    @Builder.Default
    private String preferredLanguage = "EN"; // Default to English

    @Column(name = "time_zone", nullable = false, length = 30)
    @Builder.Default
    private String timeZone = "Asia/Dubai"; // Default to UAE standard time

    @Column(name = "mfa_preference", nullable = false, length = 20)
    @Builder.Default
    private String mfaPreference = "EMAIL"; // e.g., NONE, EMAIL, SMS, APP

    @Column(name = "notification_preference", nullable = false, length = 20)
    @Builder.Default
    private String notificationPreference = "BOTH"; // e.g., EMAIL, SMS, BOTH, NONE
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_user_company"))
    private Company company; 

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_ur_user")),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_ur_role"))
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "first_time_login", nullable = false)
    @Builder.Default
    private boolean firstTimeLogin = true; // Default to true for newly seeded/onboarded users

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