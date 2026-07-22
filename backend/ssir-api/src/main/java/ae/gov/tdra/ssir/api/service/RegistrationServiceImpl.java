package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.CompanyDto;
import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.*;
import ae.gov.tdra.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationRequestRepository registrationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RegistrationStatusHistoryRepository historyRepository;

    @Autowired
    private DocumentStorageService storageService;

    @Override
    @Transactional
    public RegistrationRequest submitRegistrationWithFile(RegistrationRequestDto dto, MultipartFile file) {
        CompanyDto companyDto = dto.getCompany();

        // 1. Validate duplicates
        if (companyRepository.findByTradeLicenseNumber(companyDto.getTradeLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("A company with this Trade License Number is already registered");
        }

        // 2. Build Company
        Company company = Company.builder()
                .companyName(companyDto.getCompanyName())
                .legalEntityName(companyDto.getLegalEntityName())
                .tradeLicenseNumber(companyDto.getTradeLicenseNumber())
                .registrationNumber(companyDto.getRegistrationNumber())
                .taxVatNumber(companyDto.getTaxId())
                .companyType(companyDto.getCompanyType())
                .industryType(companyDto.getIndustry())
                .dateOfIncorporation(companyDto.getDateOfIncorporation())
                .email(companyDto.getCompanyEmail())
                .companyPhone(companyDto.getCompanyPhone())
                .website(companyDto.getWebsite())
                .status("DRAFT")
                .build();

        // 3. Build Address
        CompanyAddress address = CompanyAddress.builder()
                .company(company)
                .addressLine1(companyDto.getRegisteredAddress())
                .country(companyDto.getCountry())
                .emirate(companyDto.getEmirateState())
                .city(companyDto.getCity())
                .postalCode(companyDto.getPostalCode())
                .build();
        
        company.setAddress(address);

        // 4. Build Registration Request
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        RegistrationRequest request = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(company)
                .currentStatus("SUBMITTED")
                .build();

        // Persist registration and company first to obtain primary key
        RegistrationRequest savedRequest = registrationRepository.save(request);

        // 5. Upload file directly to S3 and link it to this registration
        if (file != null && !file.isEmpty()) {
            storageService.uploadAndLinkDocument(file, "TRADE_LICENSE", savedRequest);
        }

        // 6. Save History Log
        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(savedRequest)
                .fromStatus("DRAFT")
                .toStatus("SUBMITTED")
                .comments("Initial Onboarding Request Submitted")
                .build();
        
        historyRepository.save(history);

        return savedRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationRequest> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Override
    @Transactional
    public RegistrationRequest updateRegistrationStatus(Long id, String status, String comments) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        String oldStatus = request.getCurrentStatus();
        request.setCurrentStatus(status);

        if ("APPROVED".equalsIgnoreCase(status)) {
            request.getCompany().setStatus("ACTIVE");
            String companyIdStr = "COMP" + String.format("%06d", request.getCompany().getId());
            request.getCompany().setCompanyId(companyIdStr);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            request.getCompany().setStatus("DEACTIVATED");
            request.setRejectionReason(comments);
        }

        RegistrationRequest updatedRequest = registrationRepository.save(request);

        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(updatedRequest)
                .fromStatus(oldStatus)
                .toStatus(status)
                .comments(comments)
                .build();
        
        historyRepository.save(history);

        return updatedRequest;
    }
}