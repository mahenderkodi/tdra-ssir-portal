package ae.gov.tdra.ssir.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // Import Jackson Property Annotation
import lombok.Data;

@Data
public class AccountDto {


    private String username;
    
   
    private String preferredLanguage;

  
    private String timeZone;

  
    private String mfaPreference;

   
    private String notificationPreference;
}