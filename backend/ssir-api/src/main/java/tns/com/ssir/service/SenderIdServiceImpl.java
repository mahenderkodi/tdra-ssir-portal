package tns.com.ssir.service;

import tns.com.ssir.dto.CompanyDashboardStats;
import tns.com.ssir.dto.SenderIdRequestDto;
import tns.com.ssir.dto.SenderIdResponseDto;
import tns.com.ssir.core.entity.AuditLog;
import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.SenderId;
import tns.com.ssir.core.repository.AuditLogRepository;
import tns.com.ssir.core.repository.CompanyRepository;
import tns.com.ssir.core.repository.SenderIdRepository;
import tns.com.ssir.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SenderIdServiceImpl implements SenderIdService {

    @Autowired
    private SenderIdRepository senderIdRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentStorageService storageService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanyDashboardStats getDashboardStats(Long companyId) {
        log.info("Calculating secure compliance metrics for Company ID: {}", companyId);

        long activeSenderIds = senderIdRepository.countByCompanyIdAndStatus(companyId, "ACTIVE");
        
        long criticalWarnings = senderIdRepository.countByCompanyIdAndStatusIn(
                companyId, 
                List.of("EXPIRING_SOON", "EXPIRED")
        );
        
        long totalUsers = userRepository.countByCompanyId(companyId);

        return CompanyDashboardStats.builder()
                .activeSenderIds(activeSenderIds)
                .criticalExpiryWarnings(criticalWarnings)
                .totalUsers(totalUsers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SenderIdResponseDto> getCompanySenderIds(Long companyId) {
        log.info("Fetching Sender ID registry list for Company ID: {}", companyId);
        
        List<SenderId> senderIds = senderIdRepository.findByCompanyId(companyId);

        return senderIds.stream()
                .map(senderId -> SenderIdResponseDto.builder()
                        .id(senderId.getId())
                        .senderIdName(senderId.getSenderIdName())
                        .trackingId(senderId.getTrackingId()) 
                        .status(senderId.getStatus())
                        .createdAt(senderId.getCreatedAt())     
                        .expirationDate(senderId.getExpirationDate())
                        .remarks(senderId.getRemarks())     
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SenderIdResponseDto getSenderIdById(Long id, Long companyId) {
        log.info("Fetching detailed view of Sender ID: {} for Company ID: {}", id, companyId);

        SenderId senderId = senderIdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sender ID record not found with ID: " + id));

        // Security boundary check
        if (!senderId.getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this Sender ID record.");
        }

        // Generate short-lived presigned URL for MinIO file download [2]
        String secureDocUrl = null;
        if (senderId.getAuthLetterPath() != null) {
            secureDocUrl = storageService.generatePresignedUrl(senderId.getAuthLetterPath());
        }

        return SenderIdResponseDto.builder()
                .id(senderId.getId())
                .senderIdName(senderId.getSenderIdName())
                .trackingId(senderId.getTrackingId())
                .status(senderId.getStatus())
                .createdAt(senderId.getCreatedAt())
                .expirationDate(senderId.getExpirationDate())
                .remarks(senderId.getRemarks())
                .justification(senderId.getJustification())
                .companyName(senderId.getCompany().getCompanyName())
                .authLetterUrl(secureDocUrl) // Mapped presigned file [2]
                .build();
    }
    
    @Override
    @Transactional
    public SenderIdResponseDto requestSenderId(SenderIdRequestDto dto, MultipartFile authLetter, Long companyId) {
        log.info("Processing new Sender ID request: '{}' for Company ID: {}", dto.getSenderIdName(), companyId);

        if (senderIdRepository.findBySenderIdName(dto.getSenderIdName()).isPresent()) {
            throw new IllegalArgumentException("This Sender ID header is already registered on the platform.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        // Generate a standard Tracking ID
        String trackingId = "SND-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Persist Metadata [3]
        SenderId senderId = SenderId.builder()
                .senderIdName(dto.getSenderIdName())
                .trackingId(trackingId)
                .justification(dto.getJustification())
                .company(company)
                .status("PENDING")
                .build();

        // Stream binary file directly to MinIO and save path [2]
        if (authLetter != null && !authLetter.isEmpty()) {
            String fileStoragePath = "companies/" + companyId + "/sender_ids/" + trackingId.toLowerCase() + "_auth_letter.pdf";
            storageService.uploadDocument(authLetter, fileStoragePath);
            senderId.setAuthLetterPath(fileStoragePath); // Save file reference [2]
        }

        SenderId savedSenderId = senderIdRepository.save(senderId);

        return SenderIdResponseDto.builder()
                .id(savedSenderId.getId())
                .senderIdName(savedSenderId.getSenderIdName())
                .trackingId(savedSenderId.getTrackingId())
                .status(savedSenderId.getStatus())
                .expirationDate(savedSenderId.getExpirationDate())
                .createdAt(savedSenderId.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public SenderIdResponseDto updateSenderIdStatus(Long id, String status, String comments) {
        log.info("Executing Admin status update for Sender ID: {}. Action: {}", id, status);

        SenderId senderId = senderIdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sender ID not found with ID: " + id));

        senderId.setStatus(status.toUpperCase());
        senderId.setRemarks(comments); // Save the feedback remarks
        SenderId savedSenderId = senderIdRepository.save(senderId);

        String adminUsername = "SYSTEM";
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            adminUsername = auth.getName();
        }

        AuditLog auditLog = AuditLog.builder()
                .actorUsername(adminUsername)
                .actionTaken("SENDER_ID_" + status.toUpperCase())
                .ipAddress("127.0.0.1")
                .payloadDetails(String.format("Sender ID: %s, Comments: %s", 
                        savedSenderId.getSenderIdName(), comments))
                .build();
        auditLogRepository.save(auditLog);

        return SenderIdResponseDto.builder()
                .id(savedSenderId.getId())
                .senderIdName(savedSenderId.getSenderIdName())
                .trackingId(savedSenderId.getTrackingId())
                .status(savedSenderId.getStatus())
                .expirationDate(savedSenderId.getExpirationDate())
                .createdAt(savedSenderId.getCreatedAt())
                .remarks(savedSenderId.getRemarks())
                .build();
    }
}