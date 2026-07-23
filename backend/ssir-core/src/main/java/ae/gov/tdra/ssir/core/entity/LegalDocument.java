package ae.gov.tdra.ssir.core.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import jakarta.persistence.Transient;

@Entity
@Table(name = "legal_documents")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "registrationRequest")
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = true, foreignKey = @ForeignKey(name = "fk_doc_reg"))
    @JsonIgnore
    private RegistrationRequest registrationRequest; // Nullable during draft uploads

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; 

    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Transient
    private String presignedUrl;
    
    @Column(name = "file_storage_path", nullable = false)
    private String fileStoragePath; // Key/Path inside the MinIO bucket

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}