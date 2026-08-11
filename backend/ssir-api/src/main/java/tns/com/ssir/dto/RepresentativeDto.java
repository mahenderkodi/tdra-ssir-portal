package tns.com.ssir.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RepresentativeDto {

    @NotBlank(message = "{representative.firstName.required}")
    private String firstName;

    @NotBlank(message = "{representative.lastName.required}")
    private String lastName;

    @NotBlank(message = "{representative.designation.required}")
    private String designation;

    @NotBlank(message = "{representative.department.required}")
    private String department;

    @NotBlank(message = "{representative.officialEmail.required}")
    @Email(message = "{representative.officialEmail.invalid}")
    private String officialEmail;

    @NotBlank(message = "{representative.mobileNumber.required}")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "{representative.mobileNumber.pattern}")
    private String mobileNumber;

    @NotBlank(message = "{representative.officeNumber.required}")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "{representative.officeNumber.pattern}")
    private String officeNumber;

    @NotBlank(message = "{representative.address.required}")
    private String address;

    @NotBlank(message = "{representative.uaePassId.required}")
    private String uaePassId;

    @NotBlank(message = "{representative.passportOrEmiratesId.required}")
    private String passportOrEmiratesId;
}