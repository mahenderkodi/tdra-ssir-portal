package ae.gov.tdra.ssir.api.service;


import ae.gov.tdra.ssir.api.dto.CompanyDto;
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
        CompanyDto companyDto = dto.getCompany();

        // 1. Validate if trade license number is already registered
        if (companyRepository.findByTradeLicenseNumber(companyDto.getTradeLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("A company with this Trade License Number is already registered");
        }

        // 2. Build the Company Entity
        Company company = Company.builder()
                .companyName(companyDto.getCompanyName())
                .legalEntityName(companyDto.getLegalEntityName())
                .tradeLicenseNumber(companyDto.getTradeLicenseNumber())
                .registrationNumber(companyDto.getRegistrationNumber())
                .taxVatNumber(companyDto.getTaxId()) // Maps taxId -> taxVatNumber
                .companyType(companyDto.getCompanyType())
                .industryType(companyDto.getIndustry()) // Maps industry -> industryType
                .dateOfIncorporation(companyDto.getDateOfIncorporation())
                .email(companyDto.getCompanyEmail()) // Maps companyEmail -> email
                .companyPhone(companyDto.getCompanyPhone())
                .website(companyDto.getWebsite())
                .status("DRAFT")
                .build();

        // 3. Build the One-to-One Address Mapping
        CompanyAddress address = CompanyAddress.builder()
                .company(company)
                .addressLine1(companyDto.getRegisteredAddress()) // Maps registeredAddress -> addressLine1
                .country(companyDto.getCountry())
                .emirate(companyDto.getEmirateState()) // Maps emirateState -> emirate
                .city(companyDto.getCity())
                .postalCode(companyDto.getPostalCode())
                .build();
        
        company.setAddress(address); // Link bi-directionally

        // 4. Build the Registration Request
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        RegistrationRequest request = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(company)
                .currentStatus("SUBMITTED")
                .build();

        // Save cascade will automatically write to 'companies' and 'company_addresses'
        RegistrationRequest savedRequest = registrationRepository.save(request);

        // 5. Write Initial Entry in Status History
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
            request.getCompany().setCompanyIdString(companyIdStr);
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