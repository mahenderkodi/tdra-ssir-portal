package tns.com.ssir.service;

import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.core.entity.RegistrationRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public interface SingleShotRegistrationService {
    RegistrationRequest submitSingleShot(RegistrationRequestDto dto, MultiValueMap<String, MultipartFile> fileMap, Long userId);
}