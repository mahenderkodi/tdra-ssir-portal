package ae.gov.tdra.ssir.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompanyDto {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Legal entity name is required")
    private String legalEntityName;

    @NotBlank(message = "Trade license number is required")
    private String tradeLicenseNumber;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Tax ID is required")
    private String taxId;

    @NotBlank(message = "Company Type is required")
    private String companyType;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Date of Incorporation is required")
    private String dateOfIncorporation;

    @NotBlank(message = "Registered Address is required")
    private String registeredAddress;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Emirate/State is required")
    private String emirateState;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Postal Code is required")
    private String postalCode;

    private String website;

    @NotBlank(message = "Company Email is required")
    @Email(message = "Invalid company email format")
    private String companyEmail;

    @NotBlank(message = "Company Phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "Invalid phone format")
    private String companyPhone;
}