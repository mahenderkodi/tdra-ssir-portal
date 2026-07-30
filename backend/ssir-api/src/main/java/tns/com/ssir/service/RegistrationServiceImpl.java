package tns.com.ssir.service;

import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import tns.com.ssir.dto.AccountDto;
import tns.com.ssir.dto.CompanyDto;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RepresentativeDto;
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
    private RoleRepository roleRepository;

    @Autowired
    private RegistrationStatusHistoryRepository historyRepository;

    @Autowired
    private DocumentStorageService storageService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap) {
        CompanyDto companyDto = dto.getCompany();

        
        if (companyRepository.findByTradeLicenseNumber(companyDto.getTradeLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("A company with this Trade License Number is already registered");
        }

     
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
        

        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        RegistrationRequest request = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(company)
                .currentStatus("SUBMITTED")
                .build();

        RegistrationRequest savedRequest = registrationRepository.save(request);
        
  
        AccountDto accountDto = dto.getAccount();
        if (accountDto != null && accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty()) {
            
            // Fetch the default ROLE_COMPANY_ADMIN role from the database
            Role companyAdminRole = roleRepository.findByRoleName("ROLE_COMPANY_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Default role ROLE_COMPANY_ADMIN not found"));
            
            java.util.Set<Role> roles = new java.util.HashSet<>();
            roles.add(companyAdminRole);

            User pendingUser = User.builder()
                    .company(company)
                    .username(accountDto.getUsername())
                    .email(repDto != null ? repDto.getOfficialEmail() : companyDto.getCompanyEmail())
                    .passwordHash("PENDING_SETUP_TOKEN") 
                    .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("PENDING_ACTIVATION")
                    .roles(roles) // <-- Map the default role here!
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

    
    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
    }
    
    

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationRequest> getAllRegistrations() {
        return registrationRepository.findAll();
    }
       
    @Override
    @Transactional(readOnly = true)
    public RegistrationRequest trackApplication(String trackingId, String tradeLicenseNumber) {
        // 1. Fetch the request by tracking ID
        RegistrationRequest request = registrationRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found with Tracking ID: " + trackingId));

        // 2. DUAL-FACTOR SECURITY CHECK: Verify that the trade license matches [3]
        if (!request.getCompany().getTradeLicenseNumber().equalsIgnoreCase(tradeLicenseNumber.trim())) {
            throw new IllegalArgumentException("Access Denied: The provided Trade License Number does not match this tracking ID.");
        }

        // 3. Generate secure, temporary viewing links for their uploaded documents [1.1.2]
        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                doc.setPresignedUrl(secureUrl); // Attached in transient memory [1.1.2]
            }
        }

        return request;
    }
    
    
    @Override
    @Transactional
    public RegistrationRequest updateRegistrationStatus(Long id, String status, String comments) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        String oldStatus = request.getCurrentStatus();
        request.setCurrentStatus(status);

        if ("APPROVED".equalsIgnoreCase(status)) {
            Company company = request.getCompany();
            company.setStatus("ACTIVE");

            // A. Generate and set permanent Company ID
            String companyIdStr = "COMP" + String.format("%06d", company.getId());
            company.setCompanyId(companyIdStr);

            // B. Find the pending user account we created during submission
            User pendingUser = userRepository.findByCompany(company)
                    .orElseThrow(() -> new IllegalArgumentException("No pending user account found associated with this company"));

            // C. Generate a secure 24-hour setup token
            String secureToken = UUID.randomUUID().toString();
            PasswordResetToken onboardingToken = PasswordResetToken.builder()
                    .token(secureToken)
                    .user(pendingUser)
                    .expiryDate(LocalDateTime.now().plusHours(24)) // Valid for 24 hours
                    .build();

            tokenRepository.save(onboardingToken);

            // D. MOCK EMAIL: Print the activation link to the console so we can copy-paste it!
            System.out.println("\n=================================================================================");
            System.out.println(">>> MOCK EMAIL NOTIFICATION SYSTEM [TDRA ONBOARDING APPROVED] <<<");
            System.out.println("To: " + pendingUser.getEmail());
            System.out.println("Subject: TDRA Onboarding Approved - Create Your Password");
            System.out.println("Dear " + pendingUser.getUsername() + ",");
            System.out.println("Your company registration request has been approved.");
            System.out.println("Your Company ID is: " + companyIdStr);
            System.out.println("Your User ID is: " + pendingUser.getUserIdString());
            System.out.println("Please click the link below to set your password and activate your account:");
            System.out.println("http://localhost:4200/create-password?token=" + secureToken); // Port 4200 Angular link
            System.out.println("=================================================================================\n");
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