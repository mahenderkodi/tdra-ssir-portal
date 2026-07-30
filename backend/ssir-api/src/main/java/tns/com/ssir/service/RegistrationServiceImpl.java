package tns.com.ssir.service;

import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import tns.com.ssir.dto.AccountDto;
import tns.com.ssir.dto.CompanyDto;
import tns.com.ssir.dto.CreateCredentialsRequest;
import tns.com.ssir.dto.MockEmailDetails;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;
import tns.com.ssir.dto.RepresentativeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuditLogRepository auditLogRepository;

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
        
  
//        AccountDto accountDto = dto.getAccount();
//        if (accountDto != null && accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty()) {
//            
//            // Fetch the default ROLE_COMPANY_ADMIN role from the database
//            Role companyAdminRole = roleRepository.findByRoleName("ROLE_COMPANY_ADMIN")
//                    .orElseThrow(() -> new IllegalStateException("Default role ROLE_COMPANY_ADMIN not found"));
//            
//            java.util.Set<Role> roles = new java.util.HashSet<>();
//            roles.add(companyAdminRole);
//
//            User pendingUser = User.builder()
//                    .company(company)
//                    .username(accountDto.getUsername())
//                    .email(repDto != null ? repDto.getOfficialEmail() : companyDto.getCompanyEmail())
//                    .passwordHash("PENDING_SETUP_TOKEN") 
//                    .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
//                    .status("PENDING_ACTIVATION")
//                    .roles(roles) // <-- Map the default role here!
//                    .preferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN")
//                    .timeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai")
//                    .mfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL")
//                    .notificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH")
//                    .build();
//            
//            userRepository.save(pendingUser);
//        }
//        
        
        String rawTempPassword = "tmp" + UUID.randomUUID().toString().substring(0, 5); // e.g., tmpX9a2F
        String representativeEmail = repDto != null ? repDto.getOfficialEmail() : companyDto.getCompanyEmail();

        Role pendingRole = roleRepository.findByRoleName("ROLE_COMPANY_PENDING")
                .orElseThrow(() -> new IllegalStateException("Required system role ROLE_COMPANY_PENDING was not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(pendingRole);

        User pendingUser = User.builder()
                .company(company)
                .username(representativeEmail) // Email acts as username
                .email(representativeEmail)
                .passwordHash(passwordEncoder.encode(rawTempPassword)) // Hashed securely
                .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("PENDING_ACTIVATION") // Pending activation until approved
                .roles(roles)
                .firstTimeLogin(true) // Set firstTimeLogin flag to true [1]
                .build();
        
        userRepository.save(pendingUser);
        
        // Store plain-text temp password temporarily in the returned object metadata
        // so that the controller can retrieve it and return it in the success payload
        savedRequest.setRejectionReason(rawTempPassword); 
        
        

      
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
    @Transactional
    public User createCredentials(CreateCredentialsRequest request) {
        // 1. Fetch the associated registration request
        RegistrationRequest regRequest = registrationRepository.findByTrackingId(request.getTrackingId())
                .orElseThrow(() -> new IllegalArgumentException("Onboarding request not found with Tracking ID: " + request.getTrackingId()));

        Company company = regRequest.getCompany();

        // Prevent duplicate user registrations for the same company
        if (userRepository.findByCompany(company).isPresent()) {
            throw new IllegalArgumentException("Credentials have already been created for this company application.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        // 2. Fetch the restricted PENDING role [1]
        Role pendingRole = roleRepository.findByRoleName("ROLE_COMPANY_PENDING")
                .orElseThrow(() -> new IllegalStateException("Required system role ROLE_COMPANY_PENDING was not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(pendingRole);

        // 3. Persist the pending user
        User user = User.builder()
                .company(company)
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // BCrypt hashed immediately
                .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("PENDING_ACTIVATION") // User must be pending until Admin approves company [1]
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationRequest getRegistrationByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        RegistrationRequest request = registrationRepository.findByCompany(company)
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found associated with this company."));

        // Generate temporary pre-signed links for secure status tracking [1.1.2]
        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                doc.setPresignedUrl(secureUrl);
            }
        }
        return request;
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
    public RegistrationRequest trackApplication(String trackingId) {
        // 1. Fetch the request by tracking ID
        RegistrationRequest request = registrationRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found with Tracking ID: " + trackingId));

        // 2. DUAL-FACTOR SECURITY CHECK: Verify that the trade license matches [3]
//        if (!request.getCompany().getTradeLicenseNumber().equalsIgnoreCase(tradeLicenseNumber.trim())) {
//            throw new IllegalArgumentException("Access Denied: The provided Trade License Number does not match this tracking ID.");
//        }

        // 3. Generate secure, temporary viewing links for their uploaded documents [1.1.2]
        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                doc.setPresignedUrl(secureUrl); // Attached in transient memory [1.1.2]
            }
        }

        return request;
    }
    
    
    // --- UPDATED METHOD: Processes status transitions and constructs mock email payloads ---
    @Override
    @Transactional
    public RegistrationStatusUpdateResponse updateRegistrationStatus(Long id, String status, String comments) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        String oldStatus = request.getCurrentStatus();
        request.setCurrentStatus(status);

        String generatedCompanyId = null;
        String generatedUserId = null;
        MockEmailDetails emailDetails = null;
        boolean emailSent = false;

        if ("APPROVED".equalsIgnoreCase(status)) {
            Company company = request.getCompany();
            company.setStatus("ACTIVE");

            // A. Generate Company ID
            generatedCompanyId = "COMP" + String.format("%06d", company.getId());
            company.setCompanyId(generatedCompanyId);

            // B. Find associated pending User
            User pendingUser = userRepository.findByCompany(company)
                    .orElseThrow(() -> new IllegalArgumentException("No pending user account found associated with this company"));
            generatedUserId = pendingUser.getUserIdString();

            // C. Generate 24-hour setup token [1]
            String secureToken = UUID.randomUUID().toString();
            PasswordResetToken onboardingToken = PasswordResetToken.builder()
                    .token(secureToken)
                    .user(pendingUser)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();
            tokenRepository.save(onboardingToken);

            // D. CONSTRUCT REGULATOR APPROVAL EMAIL PAYLOAD
            String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Congratulations! Your company onboarding request has been approved by the TDRA.\n\n" +
                    "Company Registry ID: %s\n" +
                    "User Access ID: %s\n\n" +
                    "Please click the link below to set your account password and activate your portal access:\n" +
                    "http://localhost:4200/create-password?token=%s\n\n" +
                    "Regards,\nTDRA Onboarding Team",
                    pendingUser.getUsername(), generatedCompanyId, generatedUserId, secureToken
            );

            emailDetails = MockEmailDetails.builder()
                    .to(pendingUser.getEmail())
                    .subject("TDRA Onboarding Approved - Create Your Password")
                    .body(emailBody)
                    .build();
            emailSent = true;

        } else if ("REJECTED".equalsIgnoreCase(status) || "INFO_REQUESTED".equalsIgnoreCase(status)) {
            Company company = request.getCompany();
            company.setStatus("DEACTIVATED");
            
            if ("REJECTED".equalsIgnoreCase(status)) {
                request.setRejectionReason(comments);
            } else {
                request.setInfoRequestComments(comments);
            }

            // E. CONSTRUCT REGULATOR ALERT EMAIL PAYLOAD
            String emailBody = String.format(
                    "Dear Applicant,\n\n" +
                    "Your company registration request (Tracking ID: %s) has been %s.\n\n" +
                    "Reviewer Comments & Feedback:\n" +
                    "\"%s\"\n\n" +
                    "Please review the comments above and take appropriate action on the SSIR portal.\n\n" +
                    "Regards,\nTDRA Onboarding Team",
                    request.getTrackingId(), status.toLowerCase(), comments
            );

            emailDetails = MockEmailDetails.builder()
                    .to(company.getEmail())
                    .subject("TDRA Onboarding " + status + " - Tracking ID: " + request.getTrackingId())
                    .body(emailBody)
                    .build();
            emailSent = true;
        }

        RegistrationRequest updatedRequest = registrationRepository.save(request);

        // System Audit Logger
        String adminUsername = "SYSTEM";
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            adminUsername = auth.getName();
        }

        AuditLog auditLog = AuditLog.builder()
                .actorUsername(adminUsername)
                .actionTaken("REGISTRATION_" + status.toUpperCase())
                .ipAddress("127.0.0.1")
                .payloadDetails(String.format("Request ID: %d, Tracking ID: %s, Comments: %s", 
                        updatedRequest.getId(), updatedRequest.getTrackingId(), comments))
                .build();
        auditLogRepository.save(auditLog);

        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(updatedRequest)
                .fromStatus(oldStatus)
                .toStatus(status)
                .comments(comments)
                .build();
        historyRepository.save(history);

        // Return the clean, formatted response carrying your mock email details!
        return RegistrationStatusUpdateResponse.builder()
                .trackingId(updatedRequest.getTrackingId())
                .currentStatus(updatedRequest.getCurrentStatus())
                .companyId(generatedCompanyId)
                .userId(generatedUserId)
                .emailSent(emailSent)
                .mockEmailDetails(emailDetails)
                .build();
    }

}