package tns.com.ssir.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountDto {

    @NotBlank(message = "{account.username.required}")
    @Size(min = 4, max = 30, message = "{account.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{account.username.pattern}")
    private String username;

    @NotBlank(message = "{account.preferredLanguage.required}")
    private String preferredLanguage;

    @NotBlank(message = "{account.timeZone.required}")
    private String timeZone;

    @NotBlank(message = "{account.mfaPreference.required}")
    private String mfaPreference;

    @NotBlank(message = "{account.notificationPreference.required}")
    private String notificationPreference;
}