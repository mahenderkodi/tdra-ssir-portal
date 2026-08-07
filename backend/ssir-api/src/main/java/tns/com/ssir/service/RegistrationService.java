package tns.com.ssir.service;

import tns.com.ssir.dto.ForgotPasswordRequest;
import tns.com.ssir.dto.RegisterInitRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;
import tns.com.ssir.dto.ResetPasswordRequest;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.core.entity.User;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    // 1. Initial Account Registration (Publicly Whitelisted) [1]
    User registerAndInit(RegisterInitRequest request);

    // 2. Updates existing authenticated draft [1, 3]
    RegistrationRequest updateDraft(RegistrationRequestDto dto, Long companyId);

    // 3. Performs strict validation, uploads files, and finalizes submission [1, 2, 3]
    RegistrationRequest submitFinalOnboarding(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long companyId);

    RegistrationRequest getRegistrationByCompanyId(Long companyId);

    RegistrationRequest getRegistrationWithPresignedUrls(Long id);

    RegistrationStatusUpdateResponse updateRegistrationStatus(Long id, String status, String comments);

    List<RegistrationRequest> getAllRegistrations();

    RegistrationRequest trackApplication(String trackingId);

    void processForgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}