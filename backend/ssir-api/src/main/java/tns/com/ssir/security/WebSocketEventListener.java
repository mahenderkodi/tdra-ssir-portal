package tns.com.ssir.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.*;
import tns.com.ssir.core.entity.OfflineMessage;
import tns.com.ssir.core.entity.WebSocketAuditLog;
import tns.com.ssir.core.repository.OfflineMessageRepository;
import tns.com.ssir.core.repository.WebSocketAuditLogRepository;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Log4j2
public class WebSocketEventListener {

    @Autowired
    private WebSocketAuditLogRepository auditRepository;

    @Autowired
    private OfflineMessageRepository offlineMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String username = headers.getUser() != null ? headers.getUser().getName() : "ANONYMOUS";

        auditRepository.save(WebSocketAuditLog.builder()
                .sessionId(sessionId)
                .username(username)
                .action("CONNECT")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @EventListener
    @Transactional
    public void handleSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String username = headers.getUser() != null ? headers.getUser().getName() : "ANONYMOUS";
        String destination = headers.getDestination();

        log.info("User {} subscribed to destination: {} from Session ID: {}", username, destination, sessionId);

        // 1. Audit the subscription activity [3]
        auditRepository.save(WebSocketAuditLog.builder()
                .sessionId(sessionId)
                .username(username)
                .action("SUBSCRIBE")
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .build());

        // 2. Play back any missed offline messages if subscribing to their user-specific queue [2]
        if (destination != null && destination.contains("/queue/notifications") && !"ANONYMOUS".equals(username)) {
            
            // Retrieve all pending records ordered oldest-to-newest [2]
            List<OfflineMessage> pendingMessages = offlineMessageRepository.findByUsernameOrderByCreatedAtAsc(username);
            
            if (!pendingMessages.isEmpty()) {
                log.info("Found {} pending offline messages for user: {}. Launching playback...", pendingMessages.size(), username);
                
                for (OfflineMessage msg : pendingMessages) {
                    try {
                        // De-serialize JSON back to its original object structure [2]
                        Object parsedPayload = objectMapper.readValue(msg.getPayload(), Object.class);
                        
                        // Push silently down the private WebSocket connection [2]
                        messagingTemplate.convertAndSendToUser(username, msg.getDestination(), parsedPayload);
                        
                        // Audit successful delivery
                        auditRepository.save(WebSocketAuditLog.builder()
                                .sessionId(sessionId)
                                .username(username)
                                .action("PLAYBACK_DELIVERED")
                                .destination("/user/" + username + msg.getDestination())
                                .payload(msg.getPayload())
                                .timestamp(LocalDateTime.now())
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to parse and deliver offline message ID: {}", msg.getId(), e);
                    }
                }
                
                // 3. Purge the database queue for this user once delivered [2]
                offlineMessageRepository.deleteByUsername(username);
                log.info("Offline queue successfully cleared for user: {}", username);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String username = headers.getUser() != null ? headers.getUser().getName() : "ANONYMOUS";

        auditRepository.save(WebSocketAuditLog.builder()
                .sessionId(sessionId)
                .username(username)
                .action("DISCONNECT")
                .timestamp(LocalDateTime.now())
                .build());
    }
}