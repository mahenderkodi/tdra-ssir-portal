package tns.com.ssir.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SenderIdRequestDto {

    @NotBlank(message = "{senderId.senderIdName.required}")
    @Size(min = 2, max = 11, message = "{senderId.senderIdName.size}")
    private String senderIdName;

    @Size(max = 1000, message = "{senderId.justification.size}")
    private String justification;
}