package tns.com.ssir.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tns.com.ssir.dto.UaePassTestRequest;
import java.time.LocalDateTime;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth/test")
public class UaePassTestController {

    @PostMapping("/capture-code")
    @PreAuthorize("permitAll()") // Keep public for testing
    public ResponseEntity<String> verifyCodeLifespan(@RequestBody UaePassTestRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime acquiredAt = request.getAcquiredAt();

        if (acquiredAt.isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acquisition timestamp cannot be in the future.");
        }

        long secondsElapsed = Duration.between(acquiredAt, now).getSeconds();
        long tenMinutesInSeconds = 600; // 10 minutes * 60 seconds

        if (secondsElapsed > tenMinutesInSeconds) {
            String errorMsg = String.format(
                "{\"status\": \"EXPIRED\", \"elapsedSeconds\": %d, \"message\": \"Authorization code has expired (TTL exceeded 10 minutes). Please generate a new code.\"}",
                secondsElapsed
            );
            return ResponseEntity.status(HttpStatus.GONE).body(errorMsg);
        }

        long secondsRemaining = tenMinutesInSeconds - secondsElapsed;
        String successMsg = String.format(
            "{\"status\": \"ACTIVE\", \"elapsedSeconds\": %d, \"secondsRemaining\": %d, \"message\": \"Code is valid and active. Ready for token exchange.\"}",
            secondsElapsed, secondsRemaining
        );
        return ResponseEntity.ok(successMsg);
    }
}