package ae.gov.tdra.ssir.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequestDto {

    // 1. Corporate Profile Section
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Legal entity name is required")
    private String legalEntityName;

    @NotBlank(message = "Trade license number is required")
    @Size(min = 5, max = 50, message = "Trade license number must be between 5 and 50 characters")
    private String tradeLicenseNumber;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Company email is required")
    @Email(message = "Invalid company email format")
    private String email;

    private String taxVatNumber;
    private String industryType;
    private String website;

    // 2. Company Address Section
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "Emirate is required")
    private String emirate;

    @NotBlank(message = "City is required")
    private String city;

    private String postalCode;

    // 3. Authorized Contact Section
    @NotBlank(message = "Authorized representative first name is required")
    private String firstName;

    @NotBlank(message = "Authorized representative last name is required")
    private String lastName;

    @NotBlank(message = "Representative designation is required")
    private String designation;

    @NotBlank(message = "Representative email is required")
    @Email(message = "Invalid representative email format")
    private String officialEmail;

    @NotBlank(message = "Representative mobile number is required")
    private String mobileNumber;
}