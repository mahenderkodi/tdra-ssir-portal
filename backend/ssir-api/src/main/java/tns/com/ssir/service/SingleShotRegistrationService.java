package tns.com.ssir.service;

import tns.com.ssir.core.entity.Company;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public interface SingleShotRegistrationService {
    RegistrationRequest submitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId);
    
    // FIX: Removed the accidental 'findByCompany' method declaration from here [3]
    
    // NEW: Handle single-shot resubmissions after INFO_REQUESTED status transitions [1, 3]
    RegistrationRequest resubmitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId, Long companyId);
}