package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offline_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "payload", nullable = false, length = 2000)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}