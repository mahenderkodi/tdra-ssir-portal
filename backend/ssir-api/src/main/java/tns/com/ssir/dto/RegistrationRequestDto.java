package tns.com.ssir.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tns.com.ssir.dto.CompanyDto;


@Data
public class RegistrationRequestDto {

    @NotNull(message = "Company details are required")
    @Valid
    private CompanyDto company; 

    @Valid
    private DocumentsDto documents = new DocumentsDto();

    @Valid
    private RepresentativeDto representative = new RepresentativeDto();

    @Valid
    private AccountDto account = new AccountDto();
}