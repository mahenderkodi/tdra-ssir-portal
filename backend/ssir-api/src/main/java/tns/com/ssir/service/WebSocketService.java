package tns.com.ssir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.core.repository.RegistrationRequestRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RegistrationRequestRepository registrationRepository;

    // Recalculates metrics and broadcasts to connected clients in real-time [3]
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
}