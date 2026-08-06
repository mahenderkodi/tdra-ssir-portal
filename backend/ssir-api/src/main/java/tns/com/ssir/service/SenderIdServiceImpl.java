package tns.com.ssir.service;

import tns.com.ssir.dto.CompanyDashboardStats;
import tns.com.ssir.dto.SenderIdRequestDto;
import tns.com.ssir.dto.SenderIdResponseDto;
import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.SenderId;
import tns.com.ssir.core.repository.CompanyRepository;
import tns.com.ssir.core.repository.SenderIdRepository;
import tns.com.ssir.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2;
import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public CompanyDashboardStats getDashboardStats(Long companyId) {
        log.info("Calculating secure compliance metrics for Company ID: {}", companyId);

        // 1. Count active registered headers
        long activeSenderIds = senderIdRepository.countByCompanyIdAndStatus(companyId, "ACTIVE");
        
        // 2. Count critical compliance warnings (status is either EXPIRING_SOON or EXPIRED) [3]
        long criticalWarnings = senderIdRepository.countByCompanyIdAndStatusIn(
                companyId, 
                List.of("EXPIRING_SOON", "EXPIRED")
        );
        
        // 3. Count total delegated team members [1, 3]
        long totalUsers = userRepository.countByCompanyId(companyId);

        return CompanyDashboardStats.builder()
                .activeSenderIds(activeSenderIds)
                .criticalExpiryWarnings(criticalWarnings) // Mapped [3]
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
                        .status(senderId.getStatus())
                        .expirationDate(senderId.getExpirationDate())
                        .createdAt(senderId.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SenderIdResponseDto requestSenderId(SenderIdRequestDto dto, MultipartFile authLetter, Long companyId) {
        log.info("Processing new Sender ID request: '{}' for Company ID: {}", dto.getSenderIdName(), companyId);

        // 1. Prevent duplicate registrations globally
        if (senderIdRepository.findBySenderIdName(dto.getSenderIdName()).isPresent()) {
            throw new IllegalArgumentException("This Sender ID header is already registered on the platform.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        // 2. Persist the Sender ID metadata
        SenderId senderId = SenderId.builder()
                .senderIdName(dto.getSenderIdName())
                .company(company)
                .status("PENDING")
                .build();

        SenderId savedSenderId = senderIdRepository.save(senderId);

        // 3. Stream the mandatory authorization/signatory letter directly to MinIO
        if (authLetter != null && !authLetter.isEmpty()) {
            // Saves securely inside registrations/reg_{id}/sender_ids/ folder
            String docType = "SENDER_ID_AUTH_LETTER_" + savedSenderId.getSenderIdName().toUpperCase();
            storageService.uploadDocument(authLetter, docType);
        }

        return SenderIdResponseDto.builder()
                .id(savedSenderId.getId())
                .senderIdName(savedSenderId.getSenderIdName())
                .status(savedSenderId.getStatus())
                .expirationDate(savedSenderId.getExpirationDate())
                .createdAt(savedSenderId.getCreatedAt())
                .build();
    }
}