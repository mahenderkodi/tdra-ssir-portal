package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.api.dto.AccountDto;
import ae.gov.tdra.ssir.api.dto.CompanyDto;
import ae.gov.tdra.ssir.api.dto.RegistrationRequestDto;
import ae.gov.tdra.ssir.api.dto.RepresentativeDto;
import ae.gov.tdra.ssir.core.entity.*;
import ae.gov.tdra.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
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
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap) {
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
        
     
        RepresentativeDto repDto = dto.getRepresentative();
        if (repDto != null && repDto.getFirstName() != null && !repDto.getFirstName().trim().isEmpty()) {
            CompanyContact contact = CompanyContact.builder()
                    .company(company)
                    .firstName(repDto.getFirstName())
                    .lastName(repDto.getLastName())
                    .designation(repDto.getDesignation())
                    .department(repDto.getDepartment())
                    .officialEmail(repDto.getOfficialEmail())
                    .mobileNumber(repDto.getMobileNumber())
                    .officeNumber(repDto.getOfficeNumber())
                    .address(repDto.getAddress())
                    .uaePassId(repDto.getUaePassId())
                    .passportEmiratesId(repDto.getPassportOrEmiratesId())
                    .build();
            
            company.getContacts().add(contact); 
        }
        

        // 4. Build Registration Request
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        RegistrationRequest request = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(company)
                .currentStatus("SUBMITTED")
                .build();

        RegistrationRequest savedRequest = registrationRepository.save(request);
        
  
        AccountDto accountDto = dto.getAccount();
        if (accountDto != null && accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty()) {
            User pendingUser = User.builder()
                    .company(company)
                    .username(accountDto.getUsername())
                    .email(repDto != null ? repDto.getOfficialEmail() : companyDto.getCompanyEmail())
                    .passwordHash("PENDING_SETUP_TOKEN") 
                    .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("PENDING_ACTIVATION")
                    .preferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN")
                    .timeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai")
                    .mfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL")
                    .notificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH")
                    .build();
            
            userRepository.save(pendingUser);
        }
        

      
        if (fileMap != null && !fileMap.isEmpty()) {
            for (String formKey : fileMap.keySet()) {
                List<MultipartFile> files = fileMap.get(formKey);
                if (files != null) {
                    for (MultipartFile file : files) {
                        if (file != null && !file.isEmpty()) {
                            // Converts form key 'tradeLicense' to database 'TRADE_LICENSE'
                            String documentType = convertCamelCaseToUnderscore(formKey).toUpperCase();
                            storageService.uploadAndLinkDocument(file, documentType, savedRequest);
                        }
                    }
                }
            }
        }
        
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
    public RegistrationRequest getRegistrationWithPresignedUrls(Long id) {
       
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                doc.setPresignedUrl(secureUrl); 
            }
        }

        return request;
    }

    // Helper method to convert 'tradeLicense' -> 'TRADE_LICENSE'
    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
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