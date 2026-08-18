package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingDetailResponseDto {
    private Long id;                  // Registration Request ID
    private String trackingId;
    private String currentStatus;
    private LocalDateTime submittedAt;

    // Company Details
    private String companyName;
    private String legalEntityName;
    private String tradeLicenseNumber;
    private String registrationNumber;
    private String taxVatNumber;
    private String companyType;
    private String industryType;
    private LocalDate dateOfIncorporation;
    private String email;
    private String companyPhone;
    private String website;

    // Address
    private String addressLine1;
    private String country;
    private String emirate;
    private String city;
    private String postalCode;

    // Representative
    private String repFirstName;
    private String repLastName;
    private String repDesignation;
    private String repDepartment;
    private String repOfficialEmail;
    private String repMobileNumber;
    private String repOfficeNumber;
    private String repAddress;
    private String repUaePassId;
    private String repPassportEmiratesId;

    // The SPECIFIC Sender ID details being audited [3]
    private String proposedSenderId;
    private String senderIdStatus;
    private String remarks;

    // Document File Reference
    private String documentFileName;
    private String documentUrl; // Presigned URL [2]
}