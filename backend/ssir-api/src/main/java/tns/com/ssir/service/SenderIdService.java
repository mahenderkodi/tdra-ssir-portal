package tns.com.ssir.service;

import tns.com.ssir.dto.CompanyDashboardStats;
import tns.com.ssir.dto.SenderIdRequestDto;
import tns.com.ssir.dto.SenderIdResponseDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface SenderIdService {
    
    // Calculates secure corporate metrics
    CompanyDashboardStats getDashboardStats(Long companyId);
    
    // Fetches all active headers belonging to the company
    List<SenderIdResponseDto> getCompanySenderIds(Long companyId);
    
    // Submits a new Sender ID request along with an authorization file
    SenderIdResponseDto requestSenderId(SenderIdRequestDto dto, MultipartFile authLetter, Long companyId);
    
    SenderIdResponseDto updateSenderIdStatus(Long id, String status, String comments);
}