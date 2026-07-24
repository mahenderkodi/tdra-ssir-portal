package ae.gov.tdra.ssir.api.dto;

import lombok.Data;

@Data
public class RepresentativeDto {
    private String firstName;
    private String lastName;
    private String designation;
    private String department;
    private String officialEmail;
    private String mobileNumber;
    private String officeNumber;
    private String address;
    private String uaePassId;
    private String passportOrEmiratesId;
}