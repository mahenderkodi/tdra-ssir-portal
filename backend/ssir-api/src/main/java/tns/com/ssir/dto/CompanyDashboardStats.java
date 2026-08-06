package tns.com.ssir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyDashboardStats {
	private long activeSenderIds;        // Mapped to ACTIVE
    private long criticalExpiryWarnings; // Mapped to EXPIRING_SOON and EXPIRED [3]
    private long totalUsers;             // Mapped to total company users [1, 3]
}