package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.core.entity.*;
import ae.gov.tdra.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional
    public RegistrationRequest submitRegistration(RegistrationRequestDto dto) {
        
        // 1. Validate if trade license number is already registered
        if (companyRepository.findByTradeLicenseNumber(dto.getTradeLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("A company with this Trade License Number is already registered");
        }

        // 2. Build the Company Entity
        Company company = Company.builder()
                .companyName(dto.getCompanyName())
                .legalEntityName(dto.getLegalEntityName())
                .tradeLicenseNumber(dto.getTradeLicenseNumber())
                .registrationNumber(dto.getRegistrationNumber())
                .taxVatNumber(dto.getTaxVatNumber())
                .industryType(dto.getIndustryType())
                .website(dto.getWebsite())
                .email(dto.getEmail())
                .status("DRAFT") // Initial corporate status
                .build();

        // 3. Build the One-to-One Address Mapping
        CompanyAddress address = CompanyAddress.builder()
                .company(company)
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .emirate(dto.getEmirate())
                .city(dto.getCity())
                .postalCode(dto.getPostalCode())
                .build();
        
        company.setAddress(address); // Link bi-directionally

        // 4. Build the One-to-Many Authorized Contact Mapping
        CompanyContact contact = CompanyContact.builder()
                .company(company)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .designation(dto.getDesignation())
                .officialEmail(dto.getOfficialEmail())
                .mobileNumber(dto.getMobileNumber())
                .build();
        
        company.getContacts().add(contact); // Link bi-directionally

        // 5. Build the Registration Request
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        RegistrationRequest request = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(company)
                .currentStatus("SUBMITTED")
                .build();

        // Save cascade will automatically write to 'companies', 'company_addresses', & 'company_contacts'
        RegistrationRequest savedRequest = registrationRepository.save(request);

        // 6. Write Initial Entry in Status History
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
        
        // Find existing request
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        String oldStatus = request.getCurrentStatus();
        request.setCurrentStatus(status);

        // Update corresponding company status on approval
        if ("APPROVED".equalsIgnoreCase(status)) {
            request.getCompany().setStatus("ACTIVE");
            // Generate standard Company ID reference
            String companyIdStr = "COMP" + String.format("%06d", request.getCompany().getId());
            request.getCompany().setCompanyIdString(companyIdStr);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            request.getCompany().setStatus("DEACTIVATED");
            request.setRejectionReason(comments);
        }

        RegistrationRequest updatedRequest = registrationRepository.save(request);

        // Save transaction history log
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