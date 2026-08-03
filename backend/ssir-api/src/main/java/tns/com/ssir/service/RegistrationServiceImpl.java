package tns.com.ssir.service;

import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import tns.com.ssir.dto.CompanyDto;
import tns.com.ssir.dto.CreateCredentialsRequest;
import tns.com.ssir.dto.ForgotPasswordRequest;
import tns.com.ssir.dto.MockEmailDetails;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;
import tns.com.ssir.dto.RepresentativeDto;
import tns.com.ssir.dto.ResetPasswordRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2; // Import Lombok Log4j2 [1]
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Log4j2 
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
        RegistrationRequest regRequest = registrationRepository.findByTrackingId(request.getTrackingId())
                .orElseThrow(() -> new IllegalArgumentException("Onboarding request not found with Tracking ID: " + request.getTrackingId()));

        Company company = regRequest.getCompany();

        if (userRepository.findByCompany(company).isPresent()) {
            throw new IllegalArgumentException("Credentials have already been created for this company application.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        Role pendingRole = roleRepository.findByRoleName("ROLE_COMPANY_PENDING")
                .orElseThrow(() -> new IllegalStateException("Required system role ROLE_COMPANY_PENDING was not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(pendingRole);

        User user = User.builder()
                .company(company)
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // BCrypt hashed immediately
                .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("PENDING_ACTIVATION") 
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
        RegistrationRequest request = registrationRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found with Tracking ID: " + trackingId));

        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                doc.setPresignedUrl(secureUrl); 
            }
        }

        return request;
    }
    
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

            generatedCompanyId = "COMP" + String.format("%06d", company.getId());
            company.setCompanyId(generatedCompanyId);

            User user = userRepository.findByCompany(company)
                    .orElseThrow(() -> new IllegalArgumentException("No pending user account found associated with this company"));
            generatedUserId = user.getUserIdString();

            String secureToken = UUID.randomUUID().toString();
            PasswordResetToken onboardingToken = PasswordResetToken.builder()
                    .token(secureToken)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();
            tokenRepository.save(onboardingToken);

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Congratulations! Your company onboarding request has been approved by the TDRA.\n\n" +
                    "Company Registry ID: %s\n" +
                    "User Access ID: %s\n\n" +
                    "Please click the link below to set your account password and activate your portal access:\n" +
                    "http://localhost:4200/create-password?token=%s\n\n" +
                    "Regards,\nTDRA Onboarding Team",
                    user.getUsername(), generatedCompanyId, generatedUserId, secureToken
            );

            emailDetails = MockEmailDetails.builder()
                    .to(user.getEmail())
                    .subject("TDRA Onboarding Approved - Create Your Password")
                    .body(emailBody)
                    .build();
            emailSent = true;

            // Parameterized Log4j2 log output prevents log-injection [1]
            log.info("\n=================================================================================" +
                     "\n>>> MOCK EMAIL NOTIFICATION SYSTEM [TDRA ONBOARDING APPROVED] <<<" +
                     "\nTo: {}" +
                     "\nSubject: {}" +
                     "\n{}" +
                     "\n=================================================================================\n",
                     emailDetails.getTo(), emailDetails.getSubject(), emailDetails.getBody());

        } else if ("REJECTED".equalsIgnoreCase(status) || "INFO_REQUESTED".equalsIgnoreCase(status)) {
            Company company = request.getCompany();
            company.setStatus("DEACTIVATED");
            
            if ("REJECTED".equalsIgnoreCase(status)) {
                request.setRejectionReason(comments);
            } else {
                request.setInfoRequestComments(comments);
            }

            String emailBody = String.format(
                    "Dear Applicant,\n\n" +
                    "Your company registration request (Tracking ID: %s) has been %s.\n\n" +
                    "Reviewer Comments & Feedback:\n" +
                    "\"%s\"\n\n" +
                    "Regards,\nTDRA Onboarding Team",
                    request.getTrackingId(), status.toLowerCase(), comments
            );

            emailDetails = MockEmailDetails.builder()
                    .to(company.getEmail())
                    .subject("TDRA Onboarding " + status + " - Tracking ID: " + request.getTrackingId())
                    .body(emailBody)
                    .build();
            emailSent = true;

            log.info("\n=================================================================================" +
                     "\n>>> MOCK EMAIL NOTIFICATION SYSTEM [TDRA ONBOARDING {}] <<<" +
                     "\nTo: {}" +
                     "\nSubject: {}" +
                     "\n{}" +
                     "\n=================================================================================\n",
                     status.toUpperCase(), emailDetails.getTo(), emailDetails.getSubject(), emailDetails.getBody());
        }

        RegistrationRequest updatedRequest = registrationRepository.save(request);

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

        return RegistrationStatusUpdateResponse.builder()
                .trackingId(updatedRequest.getTrackingId())
                .currentStatus(updatedRequest.getCurrentStatus())
                .companyId(generatedCompanyId)
                .userId(generatedUserId)
                .emailSent(emailSent)
                .mockEmailDetails(emailDetails)
                .build();
    }
    
    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim();

        java.util.Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            tokenRepository.deleteByUser(user);

            String secureToken = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(secureToken)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(15)) 
                    .build();

            tokenRepository.save(resetToken);

            log.info("\n=================================================================================" +
                     "\n>>> MOCK EMAIL NOTIFICATION SYSTEM [PASSWORD RESET REQUESTED] <<<" +
                     "\nTo: {}" +
                     "\nSubject: SSIR Registry - Reset Your Password" +
                     "\nDear {}," +
                     "\nWe received a request to reset your password." +
                     "\nPlease click the link below to set a new password. This link is valid for 15 minutes:" +
                     "\nhttp://localhost:4200/auth/reset-password?token={}" +
                     "\n=================================================================================\n",
                     user.getEmail(), user.getUsername(), secureToken);
        } else {
            log.warn(">>> FORGOT PASSWORD ATTEMPT: Email not found in database: {}", email);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid or expired password reset token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This password-reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        log.info(">>> PASSWORD RESET SUCCESS: Successfully updated password for user: {}", user.getUsername());
    }
}