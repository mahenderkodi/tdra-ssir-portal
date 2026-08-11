package tns.com.ssir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException; 
import jakarta.validation.Validator;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationSuccessResponse;
import tns.com.ssir.dto.TrackingResponse;
import tns.com.ssir.dto.TrackedDocumentDto;
import tns.com.ssir.dto.RegistrationStatusUpdateResponse;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.service.RegistrationService;
import tns.com.ssir.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private Validator validator;

    // INJECTED: Spring-managed ObjectMapper [3]
    @Autowired
    private ObjectMapper objectMapper;

    // A. SECURE AUTHENTICATED DRAFT UPDATE
    @PutMapping("/draft")
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> updateDraft(
            @RequestBody RegistrationRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        RegistrationRequest updatedRequest = registrationService.updateDraft(dto, principal.getId(), principal.getCompanyId());

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(updatedRequest.getTrackingId())
                .status(updatedRequest.getCurrentStatus())
                .message("Your registration draft has been successfully updated.")
                .submittedAt(updatedRequest.getUpdatedAt())
                .build();

        return ResponseEntity.ok(successResponse);
    }

    // B. SECURE AUTHENTICATED SUBMISSION (POST)
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> submitFinalOnboarding(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        // FIX: Replaced manual initialization with injected mapper
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        Set<ConstraintViolation<RegistrationRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); 
        }

        RegistrationRequest savedRequest = registrationService.submitFinalOnboarding(dto, request.getMultiFileMap(), principal.getId(), principal.getCompanyId());

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully submitted to TDRA.")
                .proposedSenderId(savedRequest.getCompany().getProposedSenderId())
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    // I. SECURE AUTHENTICATED RESUBMISSION (PUT)
    @PutMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> resubmitOnboarding(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        // FIX: Replaced manual initialization with injected mapper [3]
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        Set<ConstraintViolation<RegistrationRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); 
        }

        RegistrationRequest savedRequest = registrationService.submitFinalOnboarding(dto, request.getMultiFileMap(), principal.getId(), principal.getCompanyId());

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully resubmitted to TDRA.")
                .proposedSenderId(savedRequest.getCompany().getProposedSenderId())
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return ResponseEntity.ok(successResponse);
    }

    // C. SECURE STATUS TRACKING API
    @GetMapping("/my-status")
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<TrackingResponse> getMyStatus(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user account.");
        }

        RegistrationRequest request = registrationService.getRegistrationByCompanyId(principal.getCompanyId());

        List<TrackedDocumentDto> docDtos = new java.util.ArrayList<>();
        if (request.getDocuments() != null) {
            for (tns.com.ssir.core.entity.LegalDocument doc : request.getDocuments()) {
                docDtos.add(TrackedDocumentDto.builder()
                        .documentType(doc.getDocumentType())
                        .fileName(doc.getFileName())
                        .presignedUrl(doc.getPresignedUrl())
                        .build());
            }
        }

        String feedback = null;
        if ("REJECTED".equalsIgnoreCase(request.getCurrentStatus())) {
            feedback = request.getRejectionReason();
        } else if ("INFO_REQUESTED".equalsIgnoreCase(request.getCurrentStatus())) {
            feedback = request.getInfoRequestComments();
        }

        TrackingResponse trackingResponse = TrackingResponse.builder()
                .trackingId(request.getTrackingId())
                .companyName(request.getCompany().getCompanyName())
                .currentStatus(request.getCurrentStatus())
                .submittedAt(request.getCreatedAt())
                .feedbackComments(feedback)
                .documents(docDtos)
                .build();

        return ResponseEntity.ok(trackingResponse);
    }

    // D. GET ALL REGISTRATIONS (Admin View Queue)
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TDRA_SUPER_ADMIN', 'ROLE_TDRA_REVIEWER')")
    public ResponseEntity<List<RegistrationRequest>> getAllRegistrations() {
        List<RegistrationRequest> list = registrationService.getAllRegistrations();
        return ResponseEntity.ok(list);
    }

    // E. GET SINGLE REGISTRATION BY ID (Admin Inspection View)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TDRA_SUPER_ADMIN', 'ROLE_TDRA_REVIEWER')")
    public ResponseEntity<RegistrationRequest> getRegistrationById(@PathVariable("id") Long id) {
        RegistrationRequest request = registrationService.getRegistrationWithPresignedUrls(id);
        return ResponseEntity.ok(request);
    }

    // F. UPDATE STATUS
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_TDRA_SUPER_ADMIN') or hasRole('ROLE_TDRA_APPROVER')")
    public ResponseEntity<RegistrationStatusUpdateResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments) {
        
        RegistrationStatusUpdateResponse response = registrationService.updateRegistrationStatus(id, status, comments);
        return ResponseEntity.ok(response);
    }
    
    // G. SECURE DRAFT DOCUMENT UPLOAD
    @PostMapping(value = "/draft/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> uploadDraftDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("documentType") String documentType,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty or missing.");
        }

        RegistrationRequest updatedRequest = registrationService.uploadDraftDocument(file, documentType, principal.getCompanyId());

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(updatedRequest.getTrackingId())
                .status(updatedRequest.getCurrentStatus())
                .message("Document draft successfully uploaded.")
                .build();

        return ResponseEntity.ok(successResponse);
    }
    
    // H. SECURE DRAFT RETRIEVAL ENDPOINT
    @GetMapping("/my-draft")
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationRequest> getMyDraft(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No company associated with this user.");
        }
        RegistrationRequest request = registrationService.getRegistrationByCompanyId(principal.getCompanyId());
        return ResponseEntity.ok(request);
    }
}