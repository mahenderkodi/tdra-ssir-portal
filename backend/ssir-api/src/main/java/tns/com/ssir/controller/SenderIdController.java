package tns.com.ssir.controller;

import tns.com.ssir.dto.ChartDataDto;
import tns.com.ssir.dto.CompanyDashboardStats;
import tns.com.ssir.dto.SenderIdRequestDto;
import tns.com.ssir.dto.SenderIdResponseDto;
import tns.com.ssir.service.SenderIdService;
import tns.com.ssir.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/sender-ids")
public class SenderIdController {

    @Autowired
    private SenderIdService senderIdService;

    @Autowired
    private Validator validator;

    // 1. Get Corporate Dashboard Metrics [3]
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER')")
    public ResponseEntity<CompanyDashboardStats> getDashboardStats(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user.");
        }
        CompanyDashboardStats stats = senderIdService.getDashboardStats(principal.getCompanyId());
        return ResponseEntity.ok(stats);
    }

    // 2. Get All Sender IDs belonging to the Company (Updated for List Requirements) [3]
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER')")
    public ResponseEntity<List<SenderIdResponseDto>> getCompanySenderIds(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user.");
        }
        List<SenderIdResponseDto> list = senderIdService.getCompanySenderIds(principal.getCompanyId());
        return ResponseEntity.ok(list);
    }

    // NEW: 3. Get Detailed Sender ID Record + Files [3]
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER', 'ROLE_COMPANY_VIEWER')")
    public ResponseEntity<SenderIdResponseDto> getSenderIdById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user.");
        }
        SenderIdResponseDto response = senderIdService.getSenderIdById(id, principal.getCompanyId());
        return ResponseEntity.ok(response);
    }

    // 4. Request New Sender ID (Consumes Multipart Form-Data) [3]
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_COMPANY_ADMIN', 'ROLE_COMPANY_USER')")
    public ResponseEntity<SenderIdResponseDto> requestSenderId(
            @RequestPart("senderIdData") String senderIdDataJson,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        SenderIdRequestDto dto = objectMapper.readValue(senderIdDataJson, SenderIdRequestDto.class);

        Set<ConstraintViolation<SenderIdRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        SenderIdResponseDto response = senderIdService.requestSenderId(dto, file, principal.getCompanyId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // 5. Secure Admin Action: Approve/Reject additional Sender IDs [3]
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_TDRA_SUPER_ADMIN')")
    public ResponseEntity<SenderIdResponseDto> updateSenderIdStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments) {

        SenderIdResponseDto response = senderIdService.updateSenderIdStatus(id, status, comments);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/chart/status")
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<ChartDataDto> getStatusChart(@AuthenticationPrincipal UserPrincipal principal) {
        ChartDataDto chartData = senderIdService.getStatusChartData(principal.getCompanyId());
        return ResponseEntity.ok(chartData);
    }
    
}