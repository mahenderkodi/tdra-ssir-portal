package tns.com.ssir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderIdRequestDto {

    @NotBlank(message = "Sender ID name is required.")
    @Size(min = 2, max = 11, message = "Sender ID must be between 2 and 11 characters.")
    private String senderIdName;

    // Added to accept explanation text during creation
    @Size(max = 1000, message = "Justification must not exceed 1000 characters.")
    private String justification;
}