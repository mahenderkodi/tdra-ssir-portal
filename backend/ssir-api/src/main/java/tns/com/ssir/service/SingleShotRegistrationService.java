package tns.com.ssir.service;

import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.dto.AdminRegistrationResponseDto;
import tns.com.ssir.dto.OnboardingDetailResponseDto;
import tns.com.ssir.dto.RegistrationRequestDto;

import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public interface SingleShotRegistrationService {
    RegistrationRequest submitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId);
    
    // FIX: Removed the accidental 'findByCompany' method declaration from here [3]
    
    // NEW: Handle single-shot resubmissions after INFO_REQUESTED status transitions [1, 3]
    RegistrationRequest resubmitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId, Long requestId);
    
    // Get all requests with dynamically resolved individual Sender IDs for TDRA Admin [3]
    List<AdminRegistrationResponseDto> getAllOnboardingRequests();

    // Get single detailed request with pre-signed MinIO document links [2, 3]
   // RegistrationRequest getOnboardingRequestById(Long id);
    
 // inside tns.com.ssir.service.SingleShotRegistrationService:
    OnboardingDetailResponseDto getOnboardingRequestById(Long id);
}