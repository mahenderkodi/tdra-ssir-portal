package tns.com.ssir.service;

import tns.com.ssir.dto.*;
import tns.com.ssir.core.entity.*;
import tns.com.ssir.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
    public RegistrationRequest submitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId) {
        log.info("Processing brand-new Sender ID registration for User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user session not found."));

        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();
        AccountDto accountDto = dto.getAccount();

        // 1. Get or Create the Parent Company entity [3]
        Company company = user.getCompany();
        boolean isNewCompany = (company == null);

        if (isNewCompany) {
            // Check if another user has already registered this physical company via its Trade License [3]
            String tradeLicense = companyDto.getTradeLicenseNumber();
            java.util.Optional<Company> existingCompanyOpt = companyRepository.findByTradeLicenseNumber(tradeLicense);

            if (existingCompanyOpt.isPresent()) {
                log.info("Company already registered. Mapping new user to existing Company ID: {}", existingCompanyOpt.get().getId());
                company = existingCompanyOpt.get();
                isNewCompany = false; // Treat as an existing company update
            } else {
                log.info("No company associated with user and no matching trade license found. Creating a new Company record...");
                String generatedCompanyId = "CO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                company = Company.builder()
                        .companyId(generatedCompanyId)
                        .status("DRAFT")
                        .build();
            }
        } else {
            log.info("Existing company found (ID: {}). Updating company details...", company.getId());
        }

        // 2. Populate Company fields (Updates your legal profile without duplicate entries) [3]
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

        // Map Address
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

        // 3. Populate/Update Representative Contacts [3]
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

        // 4. Link active User to company [1, 3]
        user.setCompany(savedCompany);
        
        if (accountDto != null) {
            user.setUsername(accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty() ? accountDto.getUsername() : user.getUsername());
            user.setPreferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN");
            user.setTimeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai");
            user.setMfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL");
            user.setNotificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH");
        }
        userRepository.save(user);

        // 5. Always generate a brand-new RegistrationRequest for this new Sender ID [3]
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(savedCompany)
                .currentStatus("SUBMITTED")
                .build();
        RegistrationRequest savedRequest = registrationRepository.save(regRequest);

        // 6. Always create a brand-new PENDING record inside sender_ids linked to this trackingId [3]
        if (savedCompany.getProposedSenderId() != null && !savedCompany.getProposedSenderId().trim().isEmpty()) {
            SenderId pendingSenderId = SenderId.builder()
                    .senderIdName(savedCompany.getProposedSenderId())
                    .trackingId(trackingId) // Mapped directly to this specific request's tracking ID [3]
                    .company(savedCompany)   // Mapped to the parent company [3]
                    .status("PENDING")       // Initial status is PENDING [3]
                    .build();
            senderIdRepository.save(pendingSenderId);
        }

        // 7. Stream files directly to MinIO [2]
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

        // 8. Save Status History Log [3]
        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(savedRequest)
                .fromStatus("DRAFT")
                .toStatus("SUBMITTED")
                .comments("Onboarding Application Successfully Submitted for New Sender ID")
                .build();
        historyRepository.save(history);

        return savedRequest;
    }

    @Override
    @Transactional
    public RegistrationRequest resubmitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId) {
        log.info("Processing single-shot onboarding resubmission for Company ID: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();
        AccountDto accountDto = dto.getAccount();

        // 1. Update company details
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

        // 2. Locate the active registration request currently in INFO_REQUESTED status for this company [3]
        List<RegistrationRequest> requests = registrationRepository.findByCompany(savedCompany);
        RegistrationRequest targetRequest = requests.stream()
                .filter(r -> "INFO_REQUESTED".equalsIgnoreCase(r.getCurrentStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active 'INFO_REQUESTED' onboarding request found for this company."));

        String oldStatus = targetRequest.getCurrentStatus();
        targetRequest.setCurrentStatus("SUBMITTED");
        targetRequest.setInfoRequestComments(null); // Clear previous admin comments [3]
        targetRequest.setRejectionReason(null);
        RegistrationRequest savedRequest = registrationRepository.save(targetRequest);

        // 3. Find the corresponding SenderId in PENDING/INFO_REQUESTED status and reset its details [3]
        java.util.Optional<SenderId> senderIdOpt = senderIdRepository.findByTrackingId(savedRequest.getTrackingId());
        if (senderIdOpt.isPresent()) {
            SenderId senderId = senderIdOpt.get();
            senderId.setSenderIdName(savedCompany.getProposedSenderId());
            senderId.setStatus("PENDING");
            senderId.setRemarks(null); // Clear previous remarks [3]
            senderIdRepository.save(senderId);
        }

        // 4. Stream files directly to MinIO [2]
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

        // 5. Save Status History Log [3]
        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(savedRequest)
                .fromStatus(oldStatus)
                .toStatus("SUBMITTED")
                .comments("Onboarding Application Successfully Resubmitted in Single Shot")
                .build();
        historyRepository.save(history);

        return savedRequest;
    }

    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
    }
}