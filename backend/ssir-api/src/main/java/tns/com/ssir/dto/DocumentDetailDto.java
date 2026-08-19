// DocumentsDto.java
package tns.com.ssir.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailDto {
    private String documentType;
    private String fileName;
    private String presignedUrl; // Pre-signed S3 download/view URL [2]
}
