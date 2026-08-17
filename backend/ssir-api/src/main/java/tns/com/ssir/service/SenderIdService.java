package tns.com.ssir.service;

import tns.com.ssir.dto.ChartDataDto;
import tns.com.ssir.dto.CompanyDashboardStats;
import tns.com.ssir.dto.SenderIdRequestDto;
import tns.com.ssir.dto.SenderIdResponseDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface SenderIdService {
    CompanyDashboardStats getDashboardStats(Long companyId);
    List<SenderIdResponseDto> getCompanySenderIds(Long companyId);
    SenderIdResponseDto requestSenderId(SenderIdRequestDto dto, MultipartFile authLetter, Long companyId);
    SenderIdResponseDto updateSenderIdStatus(Long id, String status, String comments);
    
    // NEW: Retrieve detailed sender ID properties and presigned documents [1, 3]
    SenderIdResponseDto getSenderIdById(Long id, Long companyId);
    
    ChartDataDto getStatusChartData(Long companyId);
}