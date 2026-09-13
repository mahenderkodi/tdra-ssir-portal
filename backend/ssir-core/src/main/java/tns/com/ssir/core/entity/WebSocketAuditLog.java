package tns.com.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "websocket_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "username")
    private String username;

    @Column(name = "action")
    private String action; 

    @Column(name = "destination")
    private String destination;

    @Column(name = "payload", length = 2000)
    private String payload;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}