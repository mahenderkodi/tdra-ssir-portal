package tns.com.ssir.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompanyDto {

    @NotBlank(message = "{company.companyName.required}")
    private String companyName;

    @NotBlank(message = "{company.legalEntityName.required}")
    private String legalEntityName;

    @NotBlank(message = "{company.tradeLicenseNumber.required}")
    private String tradeLicenseNumber;

    @NotBlank(message = "{company.registrationNumber.required}")
    private String registrationNumber;

    @NotBlank(message = "{company.taxId.required}")
    private String taxId;

    @NotBlank(message = "{company.companyType.required}")
    private String companyType;

    @NotBlank(message = "{company.industry.required}")
    private String industry;

    @NotNull(message = "{company.dateOfIncorporation.required}")
    private LocalDate dateOfIncorporation;

    @NotBlank(message = "{company.registeredAddress.required}")
    private String registeredAddress;

    private String country = "United Arab Emirates";

    @NotBlank(message = "{company.emirateState.required}")
    private String emirateState;

    @NotBlank(message = "{company.city.required}")
    private String city;

    @NotBlank(message = "{company.postalCode.required}")
    private String postalCode;

    private String website;

    @NotBlank(message = "{company.companyEmail.required}")
    @Email(message = "{company.companyEmail.invalid}")
    private String companyEmail;

    @NotBlank(message = "{company.companyPhone.required}")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "{company.companyPhone.pattern}")
    private String companyPhone;

    @NotBlank(message = "{company.proposedSenderId.required}")
    @Size(min = 2, max = 11, message = "{company.proposedSenderId.size}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{company.proposedSenderId.pattern}")
    private String proposedSenderId;
}