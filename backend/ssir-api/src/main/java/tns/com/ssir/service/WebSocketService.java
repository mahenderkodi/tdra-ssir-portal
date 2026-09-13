package tns.com.ssir.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tns.com.ssir.core.entity.OfflineMessage;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.core.entity.WebSocketAuditLog;
import tns.com.ssir.core.repository.OfflineMessageRepository;
import tns.com.ssir.core.repository.RegistrationRequestRepository;
import tns.com.ssir.core.repository.WebSocketAuditLogRepository;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpUserRegistry simpUserRegistry; // Injected to monitor real-time user connections [2]

    @Autowired
    private RegistrationRequestRepository registrationRepository;

    @Autowired
    private OfflineMessageRepository offlineMessageRepository;

    @Autowired
    private WebSocketAuditLogRepository auditRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Broadcasts real-time metrics dynamically to all connected TDRA dashboards [3]
    public void broadcastOnboardingMetrics() {
        List<RegistrationRequest> requests = registrationRepository.findAll();

        long total = requests.size();
        long pending = requests.stream().filter(r -> "SUBMITTED".equalsIgnoreCase(r.getCurrentStatus()) || "UNDER_REVIEW".equalsIgnoreCase(r.getCurrentStatus())).count();
        long approved = requests.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getCurrentStatus())).count();
        long rejected = requests.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getCurrentStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);

        messagingTemplate.convertAndSend("/topic/onboarding-stats", stats);
    }

    // NEW: Handles secure, targeted, guaranteed message dispatching [2]
    @Transactional
    public void sendGuaranteedNotification(String username, String destination, Object payload) {
        log.info("Preparing guaranteed notification for user: {} on destination: {}", username, destination);

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            jsonPayload = "{\"error\": \"Failed to serialize payload\"}";
        }

        // 1. Check if the user is currently online on any device [2]
        boolean isUserOnline = simpUserRegistry.getUser(username) != null;

        if (isUserOnline) {
            log.info("User {} is online. Pushing message instantly over WebSockets.", username);
            
            // Send immediately over the active connection [2]
            messagingTemplate.convertAndSendToUser(username, destination, payload);

            // Log successful live delivery
            auditRepository.save(WebSocketAuditLog.builder()
                    .sessionId("SYSTEM_BROADCAST")
                    .username(username)
                    .action("SEND_LIVE")
                    .destination("/user/" + username + destination)
                    .payload(jsonPayload)
                    .timestamp(LocalDateTime.now())
                    .build());
        } else {
            log.info("User {} is offline. Persisting message to MySQL offline queue...", username);
            
            // Save to MySQL so it survives server restarts and downtime [2]
            offlineMessageRepository.save(OfflineMessage.builder()
                    .username(username)
                    .destination(destination)
                    .payload(jsonPayload)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}