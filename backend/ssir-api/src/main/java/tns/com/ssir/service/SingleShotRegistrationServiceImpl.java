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

    // 1. Injected SenderIdRepository to handle the instant save of the pending header [3]
    @Autowired
    private SenderIdRepository senderIdRepository;

    @Override
    @Transactional
    public RegistrationRequest submitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId) {
        log.info("Processing single-shot onboarding submission for User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user session not found."));

        CompanyDto companyDto = dto.getCompany();
        RepresentativeDto repDto = dto.getRepresentative();
        AccountDto accountDto = dto.getAccount();

        // 2. Generate a unique Company ID String (e.g., CO-A3F1B5E8) [3]
        String generatedCompanyId = "CO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. Build and populate the Company entity completely, including the newly generated companyId [3]
        Company company = Company.builder()
                .companyId(generatedCompanyId) // Assigned [3]
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
                .proposedSenderId(companyDto.getProposedSenderId() != null ? companyDto.getProposedSenderId().toUpperCase() : null)
                .website(companyDto.getWebsite())
                .status("DRAFT") 
                .build();

        // 4. Build and link the Address entity [3]
        CompanyAddress address = CompanyAddress.builder()
                .company(company)
                .addressLine1(companyDto.getRegisteredAddress())
                .country(companyDto.getCountry() != null ? companyDto.getCountry() : "United Arab Emirates")
                .emirate(companyDto.getEmirateState())
                .city(companyDto.getCity())
                .postalCode(companyDto.getPostalCode())
                .build();
        company.setAddress(address);

        // 5. Save the Company first to capture the generated ID [3]
        Company savedCompany = companyRepository.save(company);

        // 6. Build and link Representative Contacts if available [3]
        if (repDto != null) {
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

        // 7. Link the User to the saved Company [1, 3]
        user.setCompany(savedCompany);
        
        // 8. Map and update User account preferences (Step 4) [1, 3]
        if (accountDto != null) {
            user.setUsername(accountDto.getUsername() != null && !accountDto.getUsername().trim().isEmpty() ? accountDto.getUsername() : user.getUsername());
            user.setPreferredLanguage(accountDto.getPreferredLanguage() != null ? accountDto.getPreferredLanguage() : "EN");
            user.setTimeZone(accountDto.getTimeZone() != null ? accountDto.getTimeZone() : "Asia/Dubai");
            user.setMfaPreference(accountDto.getMfaPreference() != null ? accountDto.getMfaPreference() : "EMAIL");
            user.setNotificationPreference(accountDto.getNotificationPreference() != null ? accountDto.getNotificationPreference() : "BOTH");
        }
        userRepository.save(user);

        // 9. Create the RegistrationRequest in the SUBMITTED status [3]
        String trackingId = "REG-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .trackingId(trackingId)
                .company(savedCompany)
                .currentStatus("SUBMITTED")
                .build();
        RegistrationRequest savedRequest = registrationRepository.save(regRequest);

        // 10. Automatically create a PENDING record in sender_ids table for your first proposed Sender ID [3]
        if (savedCompany.getProposedSenderId() != null && !savedCompany.getProposedSenderId().trim().isEmpty()) {
            SenderId initialSenderId = SenderId.builder()
                    .senderIdName(savedCompany.getProposedSenderId())
                    .trackingId(trackingId) // Links it to the registration tracking-id [3]
                    .company(savedCompany)   // Maps to the company [3]
                    .status("PENDING")       // Initial status is PENDING [3]
                    .build();
            senderIdRepository.save(initialSenderId);
        }

        // 11. Stream the uploaded files directly to MinIO and write them to legal_documents [2]
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

        // 12. Save Status History Log [3]
        RegistrationStatusHistory history = RegistrationStatusHistory.builder()
                .registrationRequest(savedRequest)
                .fromStatus("DRAFT")
                .toStatus("SUBMITTED")
                .comments("Onboarding Application Successfully Submitted in Single Shot")
                .build();
        historyRepository.save(history);

        return savedRequest;
    }

    private String convertCamelCaseToUnderscore(String camelCase) {
        return camelCase.replaceAll("(?<!_)(?=[A-Z])", "_");
    }
}