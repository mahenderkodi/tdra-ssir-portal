package tns.com.ssir.service;

import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.core.entity.User;
import tns.com.ssir.dto.CreateCredentialsRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RegistrationService {
    
    RegistrationRequest submitRegistrationWithFiles(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap);
    
    List<RegistrationRequest> getAllRegistrations();
    
    RegistrationStatusUpdateResponse updateRegistrationStatus(Long id, String status, String comments);
    
    RegistrationRequest getRegistrationWithPresignedUrls(Long id);
    
    RegistrationRequest trackApplication(String trackingId);
    
    User createCredentials(CreateCredentialsRequest request);
    
    RegistrationRequest getRegistrationByCompanyId(Long companyId);
    
    
}