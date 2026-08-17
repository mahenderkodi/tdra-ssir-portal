package tns.com.ssir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private List<String> labels; // e.g. ["ACTIVE", "PENDING", "EXPIRED"]
    private List<Long> data;     // e.g. [12, 3, 1]
}