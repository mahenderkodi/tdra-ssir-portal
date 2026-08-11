package tns.com.ssir.controller;

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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tns.com.ssir.dto.RegistrationRequestDto;
import tns.com.ssir.dto.RegistrationSuccessResponse;
import tns.com.ssir.core.entity.RegistrationRequest;
import tns.com.ssir.security.UserPrincipal;
import tns.com.ssir.service.SingleShotRegistrationService;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/onboarding-single")
public class SingleShotRegistrationController {

    @Autowired
    private SingleShotRegistrationService singleShotService;

    @Autowired
    private Validator validator;

    // INJECTED: Spring-managed ObjectMapper containing JSR-310 (LocalDate) deserializers [3]
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> submitOnboardingSingle(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        // FIX: Replaced manual initialization with injected mapper
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        Set<ConstraintViolation<RegistrationRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        RegistrationRequest savedRequest = singleShotService.submitSingleShot(
                dto, 
                request.getMultiFileMap(), 
                principal.getId()
        );

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully submitted.")
                .proposedSenderId(savedRequest.getCompany().getProposedSenderId())
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    // I. SECURE AUTHENTICATED SINGLE-SHOT RESUBMISSION (PUT)
    @PutMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<RegistrationSuccessResponse> resubmitOnboardingSingle(
            @RequestPart("registrationData") String registrationDataJson,
            MultipartHttpServletRequest request,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        // FIX: Replaced manual initialization with injected mapper [3]
        RegistrationRequestDto dto = objectMapper.readValue(registrationDataJson, RegistrationRequestDto.class);

        Set<ConstraintViolation<RegistrationRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        RegistrationRequest savedRequest = singleShotService.resubmitSingleShot(
                dto, 
                request.getMultiFileMap(), 
                principal.getId(),
                principal.getCompanyId()
        );

        RegistrationSuccessResponse successResponse = RegistrationSuccessResponse.builder()
                .trackingId(savedRequest.getTrackingId())
                .status(savedRequest.getCurrentStatus())
                .message("Your onboarding application has been successfully resubmitted.")
                .proposedSenderId(savedRequest.getCompany().getProposedSenderId())
                .submittedAt(savedRequest.getCreatedAt())
                .build();

        return ResponseEntity.ok(successResponse);
    }
}