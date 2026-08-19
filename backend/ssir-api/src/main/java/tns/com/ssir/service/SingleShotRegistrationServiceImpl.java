package tns.com.ssir.service;

import tns.com.ssir.dto.*;
import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SingleShotRegistrationServiceImpl implements SingleShotRegistrationService {

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
    private SenderIdRepository senderIdRepository;

    // WebSocket service
    @Autowired
    private WebSocketService webSocketService;

    @Override
    @Transactional
    public RegistrationRequest submitSingleShot(
            RegistrationRequestDto dto,
            MultiValueMap<String, MultipartFile> fileMap,
            Long userId) {

        log.info(
                "Processing brand-new Sender ID registration for User ID: {}",
                userId
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user session not found."
                        )
                );

        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();
        AccountDto accountDto = dto.getAccount();

        // ============================================================
        // 1. GET OR CREATE PARENT COMPANY
        // ============================================================

        Company company = user.getCompany();
        boolean isNewCompany = (company == null);

        if (isNewCompany) {

            String tradeLicense = companyDto.getTradeLicenseNumber();

            java.util.Optional<Company> existingCompanyOpt =
                    companyRepository.findByTradeLicenseNumber(tradeLicense);

            if (existingCompanyOpt.isPresent()) {

                log.info(
                        "Company already registered. Mapping new user to existing Company ID: {}",
                        existingCompanyOpt.get().getId()
                );

                company = existingCompanyOpt.get();
                isNewCompany = false;

            } else {

                log.info(
                        "No company associated with user and no matching trade license found. Creating a new Company record..."
                );

                String generatedCompanyId =
                        "CO-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();

                company = Company.builder()
                        .companyId(generatedCompanyId)
                        .status("DRAFT")
                        .build();
            }

        } else {

            log.info(
                    "Existing company found (ID: {}). Updating company details...",
                    company.getId()
            );
        }


        // ============================================================
        // 2. POPULATE COMPANY DETAILS
        // ============================================================

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

        company.setProposedSenderId(
                companyDto.getProposedSenderId() != null
                        ? companyDto.getProposedSenderId().toUpperCase()
                        : company.getProposedSenderId()
        );

        company.setWebsite(companyDto.getWebsite());


        // ============================================================
        // COMPANY ADDRESS
        // ============================================================

        CompanyAddress address = company.getAddress();

        if (address == null) {
            address = CompanyAddress.builder()
                    .company(company)
                    .build();
        }

        address.setAddressLine1(companyDto.getRegisteredAddress());

        address.setCountry(
                companyDto.getCountry() != null
                        ? companyDto.getCountry()
                        : "United Arab Emirates"
        );

        address.setEmirate(companyDto.getEmirateState());
        address.setCity(companyDto.getCity());
        address.setPostalCode(companyDto.getPostalCode());

        company.setAddress(address);

        Company savedCompany = companyRepository.save(company);


        // ============================================================
        // 3. UPDATE REPRESENTATIVE CONTACT
        // ============================================================

        if (repDto != null) {

            savedCompany.getContacts().clear();

            CompanyContact contact = CompanyContact.builder()
                    .company(savedCompany)
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

            savedCompany.getContacts().add(contact);

            companyRepository.save(savedCompany);
        }


        // ============================================================
        // 4. LINK USER TO COMPANY
        // ============================================================

        user.setCompany(savedCompany);

        if (accountDto != null) {

            user.setUsername(
                    accountDto.getUsername() != null &&
                    !accountDto.getUsername().trim().isEmpty()
                            ? accountDto.getUsername()
                            : user.getUsername()
            );

            user.setPreferredLanguage(
                    accountDto.getPreferredLanguage() != null
                            ? accountDto.getPreferredLanguage()
                            : "EN"
            );

            user.setTimeZone(
                    accountDto.getTimeZone() != null
                            ? accountDto.getTimeZone()
                            : "Asia/Dubai"
            );

            user.setMfaPreference(
                    accountDto.getMfaPreference() != null
                            ? accountDto.getMfaPreference()
                            : "EMAIL"
            );

            user.setNotificationPreference(
                    accountDto.getNotificationPreference() != null
                            ? accountDto.getNotificationPreference()
                            : "BOTH"
            );
        }

        userRepository.save(user);


        // ============================================================
        // 5. CREATE NEW REGISTRATION REQUEST
        // ============================================================

        String trackingId =
                "REG-" +
                LocalDateTime.now().getYear() +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        RegistrationRequest regRequest =
                RegistrationRequest.builder()
                        .trackingId(trackingId)
                        .company(savedCompany)
                        .currentStatus("SUBMITTED")
                        .build();

        RegistrationRequest savedRequest =
                registrationRepository.save(regRequest);


        // ============================================================
        // 6. CREATE PENDING SENDER ID
        // ============================================================

        if (savedCompany.getProposedSenderId() != null &&
                !savedCompany.getProposedSenderId().trim().isEmpty()) {

            SenderId pendingSenderId =
                    SenderId.builder()
                            .senderIdName(
                                    savedCompany.getProposedSenderId()
                            )
                            .trackingId(trackingId)
                            .company(savedCompany)
                            .status("PENDING")
                            .build();

            senderIdRepository.save(pendingSenderId);
        }


        // ============================================================
        // 7. UPLOAD DOCUMENTS
        // ============================================================

        if (fileMap != null && !fileMap.isEmpty()) {

            for (String formKey : fileMap.keySet()) {

                List<MultipartFile> files = fileMap.get(formKey);

                if (files != null) {

                    for (MultipartFile file : files) {

                        if (file != null && !file.isEmpty()) {

                            String documentType =
                                    convertCamelCaseToUnderscore(formKey)
                                            .toUpperCase();

                            storageService.uploadAndLinkDocument(
                                    file,
                                    documentType,
                                    savedRequest
                            );
                        }
                    }
                }
            }
        }


        // ============================================================
        // 8. SAVE STATUS HISTORY
        // ============================================================

        RegistrationStatusHistory history =
                RegistrationStatusHistory.builder()
                        .registrationRequest(savedRequest)
                        .fromStatus("DRAFT")
                        .toStatus("SUBMITTED")
                        .comments(
                                "Onboarding Application Successfully Submitted for New Sender ID"
                        )
                        .build();

        historyRepository.save(history);


        // ============================================================
        // 9. BROADCAST UPDATED METRICS
        // ============================================================

        webSocketService.broadcastOnboardingMetrics();


        // ============================================================
        // 10. RETURN
        // ============================================================

        return savedRequest;
    }


    @Override
    @Transactional
    public RegistrationRequest resubmitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId, Long requestId) {
        log.info("Processing targeted single-shot resubmission for Request ID: {} and Company ID: {}", requestId, companyId);

        // 1. Fetch the targeted RegistrationRequest directly from database by its primary key ID [3]
        RegistrationRequest targetRequest = registrationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + requestId));

        // Security boundary check: ensure the request belongs to the authenticated user's company [3]
        if (!targetRequest.getCompany().getId().equals(companyId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Unauthorized access to this registration request."
            );
        }

        // Validate that the request is actually in the correct status for resubmission [3]
        if (!"INFO_REQUESTED".equalsIgnoreCase(targetRequest.getCurrentStatus())) {
            throw new IllegalArgumentException("Registration request is not in INFO_REQUESTED status and cannot be resubmitted.");
        }

        Company company = targetRequest.getCompany();
        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();
        AccountDto accountDto = dto.getAccount();

        // 2. Update company details
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

        // Update Address
        CompanyAddress address = company.getAddress();
        if (address == null) {
            address = CompanyAddress.builder().company(company).build();
        }
        address.setAddressLine1(companyDto.getRegisteredAddress());
        address.setCountry(companyDto.getCountry() != null ? companyDto.getCountry() : "United Arab Emirates");
        address.setEmirate(companyDto.getEmirateState());
        address.setCity(companyDto.getCity());
        address.setPostalCode(companyDto.getPostalCode());
        company.setAddress(address);

        Company savedCompany = companyRepository.save(company);

        // Update Representative Contacts
        if (repDto != null) {
            savedCompany.getContacts().clear();
            CompanyContact contact = CompanyContact.builder()
                    .company(savedCompany)
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
            savedCompany.getContacts().add(contact);
            companyRepository.save(savedCompany); 
        }

        // Update User configurations
        User user = userRepository.findByCompany(savedCompany)
                .orElseThrow(() -> new IllegalArgumentException("Associated user session not found."));
        if (accountDto != null) {
            user.setUsername(accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty() ? accountDto.getUsername() : user.getUsername());
            user.setPreferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN");
            user.setTimeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai");
            user.setMfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL");
            user.setNotificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH");
            userRepository.save(user);
        }

        // 3. Transition the targeted request back to SUBMITTED status [3]
        String oldStatus = targetRequest.getCurrentStatus();
        targetRequest.setCurrentStatus("SUBMITTED");
        targetRequest.setInfoRequestComments(null); // Clear previous admin feedback [3]
        targetRequest.setRejectionReason(null);
        RegistrationRequest savedRequest = registrationRepository.save(targetRequest);

        // 4. Synchronize the specific associated SenderId in your database [3]
        java.util.Optional<SenderId> senderIdOpt = senderIdRepository.findByTrackingId(savedRequest.getTrackingId());
        if (senderIdOpt.isPresent()) {
            SenderId senderId = senderIdOpt.get();
            senderId.setSenderIdName(savedCompany.getProposedSenderId());
            senderId.setStatus("PENDING");
            senderId.setRemarks(null); // Clear previous remarks [3]
            senderIdRepository.save(senderId);
        }

        // 5. Stream any uploaded replacement files directly to MinIO [2]
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

        // 6. Save Status History Log [3]
        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(savedRequest)
                .fromStatus(oldStatus)
                .toStatus("SUBMITTED")
                .comments("Onboarding Application Successfully Resubmitted in Single Shot")
                .build();
        historyRepository.save(history);

        // 7. Broadcast the updated real-time stats to connected Admin dashboards [3]
        webSocketService.broadcastOnboardingMetrics();

        return savedRequest;
    }

    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRegistrationResponseDto> getAllOnboardingRequests() {
        log.info("Fetching all onboarding registrations for TDRA Admin Queue...");
        List<RegistrationRequest> requests = registrationRepository.findAll();

        return requests.stream()
                .map(this::mapToAdminResponseDto)
                .collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN - GET ONBOARDING REQUEST BY ID
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public OnboardingDetailResponseDto getOnboardingRequestById(Long id) {
        log.info("Fetching detailed onboarding application for ID: {}", id);
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration Request not found with ID: " + id));

        Company company = request.getCompany();

        // ============================================================
        // FIND MATCHING SENDER ID
        // ============================================================
        SenderId matchedSenderId = null;
        if (company.getSenderIds() != null) {
            matchedSenderId = company.getSenderIds().stream()
                    .filter(s -> request.getTrackingId().equals(s.getTrackingId()))
                    .findFirst()
                    .orElse(null);
        }

        String senderIdName = matchedSenderId != null ? matchedSenderId.getSenderIdName() : company.getProposedSenderId();
        String senderIdStatus = matchedSenderId != null ? matchedSenderId.getStatus() : "PENDING";
        String remarks = matchedSenderId != null ? matchedSenderId.getRemarks() : null;

        // ============================================================
        // REPRESENTATIVE DETAILS
        // ============================================================
        String repFirstName = null;
        String repLastName = null;
        String repDesignation = null;
        String repDepartment = null;
        String repOfficialEmail = null;
        String repMobileNumber = null;
        String repOfficeNumber = null;
        String repAddress = null;
        String repUaePassId = null;
        String repPassportEmiratesId = null;

        if (company.getContacts() != null && !company.getContacts().isEmpty()) {
            CompanyContact contact = company.getContacts().get(0);
            repFirstName = contact.getFirstName();
            repLastName = contact.getLastName();
            repDesignation = contact.getDesignation();
            repDepartment = contact.getDepartment();
            repOfficialEmail = contact.getOfficialEmail();
            repMobileNumber = contact.getMobileNumber();
            repOfficeNumber = contact.getOfficeNumber();
            repAddress = contact.getAddress();
            repUaePassId = contact.getUaePassId();
            repPassportEmiratesId = contact.getPassportEmiratesId();
        }

        // ============================================================
        // ADDRESS
        // ============================================================
        String addressLine1 = null;
        String country = "United Arab Emirates";
        String emirate = null;
        String city = null;
        String postalCode = null;

        if (company.getAddress() != null) {
            CompanyAddress addr = company.getAddress();
            addressLine1 = addr.getAddressLine1();
            country = addr.getCountry();
            emirate = addr.getEmirate();
            city = addr.getCity();
            postalCode = addr.getPostalCode();
        }

        // ============================================================
        // DOCUMENTS
        // ============================================================
        List<DocumentDetailDto> documentDtos = new java.util.ArrayList<>();
        if (request.getDocuments() != null) {
            for (LegalDocument doc : request.getDocuments()) {
                String secureUrl = storageService.generatePresignedUrl(doc.getFileStoragePath());
                documentDtos.add(
                        DocumentDetailDto.builder()
                                .documentType(doc.getDocumentType())
                                .fileName(doc.getFileName())
                                .presignedUrl(secureUrl)
                                .build()
                );
            }
        }

        // ============================================================
        // RESPONSE
        // ============================================================
        return OnboardingDetailResponseDto.builder()
                .id(request.getId())
                .trackingId(request.getTrackingId())
                .currentStatus(request.getCurrentStatus())
                .submittedAt(request.getCreatedAt())
                .companyName(company.getCompanyName())
                .legalEntityName(company.getLegalEntityName())
                .tradeLicenseNumber(company.getTradeLicenseNumber())
                .registrationNumber(company.getRegistrationNumber())
                .taxVatNumber(company.getTaxVatNumber())
                .companyType(company.getCompanyType())
                .industryType(company.getIndustryType())
                .dateOfIncorporation(company.getDateOfIncorporation())
                .email(company.getEmail())
                .companyPhone(company.getCompanyPhone())
                .website(company.getWebsite())
                .addressLine1(addressLine1)
                .country(country)
                .emirate(emirate)
                .city(city)
                .postalCode(postalCode)
                .repFirstName(repFirstName)
                .repLastName(repLastName)
                .repDesignation(repDesignation)
                .repDepartment(repDepartment)
                .repOfficialEmail(repOfficialEmail)
                .repMobileNumber(repMobileNumber)
                .repOfficeNumber(repOfficeNumber)
                .repAddress(repAddress)
                .repUaePassId(repUaePassId)
                .repPassportEmiratesId(repPassportEmiratesId)
                .proposedSenderId(senderIdName)
                .senderIdStatus(senderIdStatus)
                .remarks(remarks)
                .documents(documentDtos)
                .build();
    }

    // ================================================================
    // MAP ADMIN RESPONSE
    // ================================================================
    private AdminRegistrationResponseDto mapToAdminResponseDto(RegistrationRequest request) {
        Company company = request.getCompany();

        // ============================================================
        // RESOLVE SENDER ID
        // ============================================================
        String resolvedSenderId = "N/A";
        if (company.getSenderIds() != null) {
            resolvedSenderId = company.getSenderIds().stream()
                    .filter(s -> request.getTrackingId().equals(s.getTrackingId()))
                    .map(SenderId::getSenderIdName)
                    .findFirst()
                    .orElse(company.getProposedSenderId());
        }

        // ============================================================
        // REPRESENTATIVE
        // ============================================================
        String repName = "N/A";
        String repEmail = "N/A";
        if (company.getContacts() != null && !company.getContacts().isEmpty()) {
            CompanyContact contact = company.getContacts().get(0);
            repName = contact.getFirstName() + " " + contact.getLastName();
            repEmail = contact.getOfficialEmail();
        }

        // ============================================================
        // DOCUMENT COUNT
        // ============================================================
        int docCount = request.getDocuments() != null ? request.getDocuments().size() : 0;

        // ============================================================
        // RESPONSE
        // ============================================================
        return AdminRegistrationResponseDto.builder()
                .id(request.getId())
                .trackingId(request.getTrackingId())
                .companyName(company.getCompanyName())
                .companyType(company.getCompanyType())
                .proposedSenderId(resolvedSenderId)
                .representativeName(repName)
                .representativeEmail(repEmail)
                .currentStatus(request.getCurrentStatus())
                .submittedAt(request.getCreatedAt())
                .documentCount(docCount)
                .build();
    }
}