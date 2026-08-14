package tns.com.ssir.service;

import tns.com.ssir.dto.*;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.core.entity.User;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    User registerAndInit(RegisterInitRequest request);

    // Updated: Accepts both userId and companyId to support dynamic linking on first draft save [1, 3]
    RegistrationRequest updateDraft(RegistrationRequestDto dto, Long userId, Long companyId);

    // Updated: Accepts both userId and companyId to support dynamic linking on first submit [1, 2, 3]
    RegistrationRequest submitFinalOnboarding(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId);

    RegistrationRequest getRegistrationByCompanyId(Long companyId);

    RegistrationRequest getRegistrationWithPresignedUrls(Long id);

    RegistrationStatusUpdateResponse updateRegistrationStatus(Long id, String status, String comments);

    List<RegistrationRequest> getAllRegistrations();

    RegistrationRequest trackApplication(String trackingId);

    void processForgotPassword(ForgotPasswordRequest request);
    
    RegistrationRequest uploadDraftDocument(MultipartFile file, String documentType, Long companyId);

    void resetPassword(ResetPasswordRequest request);
    
    // NEW: Handle UAE PASS authentication and auto-signup [6, 7]
    AuthResponse authenticateWithUaePass(String code);
}