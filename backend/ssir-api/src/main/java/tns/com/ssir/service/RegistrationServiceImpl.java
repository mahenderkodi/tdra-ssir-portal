package tns.com.ssir.service;

import tns.com.ssir.dto.*;
import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.security.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationRequestRepository registrationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationStatusHistoryRepository historyRepository;

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SenderIdRepository senderIdRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public User registerAndInit(RegisterInitRequest request) {
        log.info("Registering initial user account: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        Role adminRole = roleRepository.findByRoleName("ROLE_COMPANY_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Required system role ROLE_COMPANY_ADMIN was not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User user = User.builder()
                .company(null) // No company exists yet [3]
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("PENDING_ACTIVATION")
                .roles(roles)
                .firstTimeLogin(false)
                .build();

        return userRepository.save(user);
    }

    private Company getOrCreateCompany(Long userId, Long companyId, String initialProposedId, String email, String companyName) {
        if (companyId != null) {
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        }

        log.info("First-time action detected. Instantiating company and linking to User ID: {}", userId);

        String finalCompanyName = (companyName != null && !companyName.trim().isEmpty()) 
                ? companyName 
                : "Draft Company " + UUID.randomUUID().toString().substring(0, 5);

        Company company = Company.builder()
                .companyName(finalCompanyName)
                .proposedSenderId(initialProposedId != null ? initialProposedId.toUpperCase() : null)
                .email(email)
                .status("DRAFT")
                .build();

        CompanyAddress address = CompanyAddress.builder()
                .company(company)
                .country("United Arab Emirates")
                .build();
        company.setAddress(address);

        Company savedCompany = companyRepository.save(company);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user session not found"));
        user.setCompany(savedCompany);
        userRepository.save(user);

        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(savedCompany)
                .currentStatus("DRAFT")
                .build();
        registrationRepository.save(regRequest);

        return savedCompany;
    }
    

    @Override
    @Transactional
    public RegistrationRequest updateDraft(RegistrationRequestDto dto, Long userId, Long companyId) {
        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();

        Company company = getOrCreateCompany(
            userId, 
            companyId, 
            companyDto.getProposedSenderId(), 
            companyDto.getCompanyEmail(),
            companyDto.getCompanyName()
        );

        company.setCompanyName(companyDto.getCompanyName());
        company.setLegalEntityName(companyDto.getLegalEntityName());
        company.setTradeLicenseNumber(companyDto.getTradeLicenseNumber());
        company.setRegistrationNumber(companyDto.getRegistrationNumber());
        company.setTaxVatNumber(companyDto.getTaxId());
        company.setCompanyType(companyDto.getCompanyType());
        company.setIndustryType(companyDto.getIndustry());
        company.setDateOfIncorporation(companyDto.getDateOfIncorporation());
        company.setEmail(companyDto.getCompanyEmail());
        company.setCompanyPhone(companyDto.getCompanyPhone());
        company.setProposedSenderId(companyDto.getProposedSenderId() != null ? companyDto.getProposedSenderId().toUpperCase() : company.getProposedSenderId());
        company.setWebsite(companyDto.getWebsite());

        CompanyAddress address = company.getAddress();
        address.setAddressLine1(companyDto.getRegisteredAddress());
        address.setCountry(companyDto.getCountry());
        address.setEmirate(companyDto.getEmirateState());
        address.setCity(companyDto.getCity());
        address.setPostalCode(companyDto.getPostalCode());

        if (repDto != null) {
            company.getContacts().clear();
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

        User user = userRepository.findByCompany(company)
                .orElseThrow(() -> new IllegalArgumentException("Associated user account not found"));

        AccountDto accountDto = dto.getAccount();
        if (accountDto != null) {
            if (accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty()) {
                user.setUsername(accountDto.getUsername());
            }
            if (accountDto.getPreferredLanguage() != null) {
                user.setPreferredLanguage(accountDto.getPreferredLanguage());
            }
            if (accountDto.getTimeZone() != null) {
                user.setTimeZone(accountDto.getTimeZone());
            }
            if (accountDto.getMfaPreference() != null) {
                user.setMfaPreference(accountDto.getMfaPreference());
            }
            if (accountDto.getNotificationPreference() != null) {
                user.setNotificationPreference(accountDto.getNotificationPreference());
            }
            userRepository.save(user);
        }

        // FIX: Extracting first element from the List using Stream mapping to satisfy new return type contract [3]
        RegistrationRequest request = registrationRepository.findByCompany(company)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found associated with this company."));

        return registrationRepository.save(request);
    }

    @Override
    @Transactional
    public RegistrationRequest submitFinalOnboarding(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId) {
        log.info("Executing final onboarding submission...");

        RegistrationRequest request = updateDraft(dto, userId, companyId);
        Company company = request.getCompany();

        String oldStatus = request.getCurrentStatus(); 

        request.setCurrentStatus("SUBMITTED");
        request.setInfoRequestComments(null);
        request.setRejectionReason(null);
        
        if (fileMap != null && !fileMap.isEmpty()) {
            for (String formKey : fileMap.keySet()) {
                List<MultipartFile> files = fileMap.get(formKey);
                if (files != null) {
                    for (MultipartFile file : files) {
                        if (file != null && !file.isEmpty()) {
                            String documentType = convertCamelCaseToUnderscore(formKey).toUpperCase();
                            storageService.uploadAndLinkDocument(file, documentType, request);
                        }
                    }
                }
            }
        }

        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(request)
                .fromStatus(oldStatus) 
                .toStatus("SUBMITTED")
                .comments("Onboarding Application Successfully Submitted for Review")
                .build();
        historyRepository.save(history);

        User user = userRepository.findByCompany(company)
                .orElseThrow(() -> new IllegalArgumentException("Associated user account not found"));
        
        AccountDto accountDto = dto.getAccount();
        if (accountDto != null) {
            user.setUsername(accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty() ? accountDto.getUsername() : user.getUsername());
            user.setPreferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN");
            user.setTimeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai");
            user.setMfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL");
            user.setNotificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH");
            userRepository.save(user);
        }

        return registrationRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationRequest getRegistrationByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        // FIX: Extracting first element from the List using Stream mapping to satisfy new return type contract [3]
        RegistrationRequest request = registrationRepository.findByCompany(company)
                .stream()
                .findFirst()
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

    @Override
    @Transactional
    public RegistrationStatusUpdateResponse updateRegistrationStatus(Long id, String status, String comments) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        String oldStatus = request.getCurrentStatus();
        request.setCurrentStatus(status);

        String generatedUserId = null;
        MockEmailDetails emailDetails = null;
        boolean emailSent = false;

        Company company = request.getCompany();

        // 1. Synchronize the associated PENDING SenderId record in your database [3]
        java.util.Optional<SenderId> senderIdOpt = senderIdRepository.findByTrackingId(request.getTrackingId());
        if (senderIdOpt.isPresent()) {
            SenderId senderId = senderIdOpt.get();
            if ("APPROVED".equalsIgnoreCase(status)) {
                senderId.setStatus("ACTIVE");
                senderId.setRemarks(comments);
                senderId.setExpirationDate(java.time.LocalDate.now().plusYears(1));
            } else {
                senderId.setStatus(status.toUpperCase()); 
                senderId.setRemarks(comments);            
            }
            senderIdRepository.save(senderId);
        }

        // 2. Perform company/user account state transitions based on the status [3]
        if ("APPROVED".equalsIgnoreCase(status)) {
            company.setStatus("ACTIVE");

            User user = userRepository.findByCompany(company)
                    .orElseThrow(() -> new IllegalArgumentException("No pending user account found associated with this company"));

            // Simply activate user (no longer execute role swaps - user remains ROLE_COMPANY_ADMIN) [1]
            user.setStatus("ACTIVE");
            userRepository.save(user);

            generatedUserId = user.getUserIdString();

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Congratulations! Your company onboarding request has been approved by the TDRA.\n\n" +
                    "Company Registry ID: %d\n" +
                    "User Access ID: %s\n\n" +
                    "Your proposed Sender ID '%s' has been successfully activated on the registry [3].\n" +
                    "You can now log in to the portal using your permanent credentials.\n\n" +
                    "Regards,\nTDRA Onboarding Team",
                    user.getUsername(), company.getId(), user.getUserIdString(), company.getProposedSenderId()
            );

            emailDetails = MockEmailDetails.builder()
                    .to(user.getEmail())
                    .subject("TDRA Onboarding Approved - Welcome to the Portal")
                    .body(emailBody)
                    .build();
            emailSent = true;

            log.info("\n=================================================================================" +
                     "\n>>> MOCK EMAIL NOTIFICATION SYSTEM [TDRA ONBOARDING APPROVED] <<<" +
                     "\nTo: {}" +
                     "\nSubject: {}" +
                     "\n{}" +
                     "\n=================================================================================\n",
                     emailDetails.getTo(), emailDetails.getSubject(), emailDetails.getBody());

        } else if ("REJECTED".equalsIgnoreCase(status) || "INFO_REQUESTED".equalsIgnoreCase(status)) {
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

        // Map and return the clean, compact REST payload [3]
        return RegistrationStatusUpdateResponse.builder()
                .trackingId(updatedRequest.getTrackingId())
                .currentStatus(updatedRequest.getCurrentStatus())
                .remarks(comments)
                .build();
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

    @Override
    @Transactional
    public RegistrationRequest uploadDraftDocument(MultipartFile file, String documentType, Long companyId) {
        log.info("Uploading draft document of type {} for Company ID: {}", documentType, companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        // FIX: Extracting first element from the List using Stream mapping to satisfy new return type contract [3]
        RegistrationRequest request = registrationRepository.findByCompany(company)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No onboarding request found associated with this company."));

        String dbDocType = documentType.toUpperCase();
        storageService.uploadAndLinkDocument(file, dbDocType, request);

        return request;
    }

    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
    }
    
    @Override
    @Transactional
    public AuthResponse authenticateWithUaePass(String code) {
        log.info("Initiating UAE PASS staging authentication handshake for code...");

        RestTemplate restTemplate = new RestTemplate();
        
        // Step A: Exchange the Authorization Code for a UAE PASS Access Token [10]
        // FIX: Update redirect_uri to point to port 4201 to resolve 'Callback url mismatch' [10]
        String tokenUrl = "https://stg-id.uaepass.ae/idshub/token"
                + "?grant_type=authorization_code"
                + "&redirect_uri=http://localhost:4201/auth/login"
                + "&code=" + code;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("sandbox_stage", "sandbox_stage");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UaePassTokenResponse> tokenResponse;

        try {
            tokenResponse = restTemplate.postForEntity(tokenUrl, entity, UaePassTokenResponse.class);
        } catch (Exception ex) {
            log.error("Failed to execute token exchange with UAE PASS: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired UAE PASS authorization code.");
        }

        UaePassTokenResponse tokenBody = tokenResponse.getBody();
        if (tokenBody == null || tokenBody.getAccessToken() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to retrieve Access Token from UAE PASS.");
        }

        String uaeAccessToken = tokenBody.getAccessToken();

        // Step B: Retrieve the user profile using the access token [7]
        String userInfoUrl = "https://stg-id.uaepass.ae/idshub/userinfo";
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(uaeAccessToken);
        HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

        ResponseEntity<UaePassUserInfo> userResponse;
        try {
            userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userEntity, UaePassUserInfo.class);
        } catch (Exception ex) {
            log.error("Failed to fetch userinfo profile from UAE PASS: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to retrieve user profile from UAE PASS.");
        }

        UaePassUserInfo userInfo = userResponse.getBody();
        if (userInfo == null || userInfo.getUuid() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empty user profile returned by UAE PASS.");
        }

        String uaeUuid = userInfo.getUuid();
        String email = userInfo.getEmail() != null ? userInfo.getEmail().trim() : "";
        String fullName = userInfo.getFullnameEN() != null ? userInfo.getFullnameEN().trim() : "UAE Pass User";

        // Step C: Check if a user with this UUID already exists in the database
        java.util.Optional<User> userOpt = userRepository.findByUaePassUuid(uaeUuid);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            log.info("Existing UAE PASS user mapped: {}", user.getUsername());
        } else {
            log.info("New UAE PASS user detected. Executing on-the-fly registration...");

            String cleanUsername = fullName.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9._-]", "_")
                    .replaceAll("_+", "_");

            if (userRepository.findByUsername(cleanUsername).isPresent()) {
                cleanUsername = cleanUsername + "_" + UUID.randomUUID().toString().substring(0, 4);
            }

            Role adminRole = roleRepository.findByRoleName("ROLE_COMPANY_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Required system role ROLE_COMPANY_ADMIN was not found."));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            String secureRandomPassword = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

            user = User.builder()
                    .uaePassUuid(uaeUuid) 
                    .username(cleanUsername)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(secureRandomPassword))
                    .userIdString("USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("ACTIVE")
                    .roles(roles)
                    .firstTimeLogin(false)
                    .build();

            user = userRepository.save(user);
        }

        // Step D: Programmatically authenticate the user into Spring Security Context and generate JWTs
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String systemAccessToken = tokenProvider.generateAccessToken(authentication);
        String systemRefreshToken = tokenProvider.generateRefreshToken(authentication);

        List<String> rolesList = principal.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(systemAccessToken)
                .refreshToken(systemRefreshToken)
                .username(user.getUsername())
                .roles(rolesList)
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .firstTimeLogin(false)
                .build();
    }
}