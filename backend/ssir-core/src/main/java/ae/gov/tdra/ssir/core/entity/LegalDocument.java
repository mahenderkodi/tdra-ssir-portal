/*
 * package ae.gov.tdra.ssir.core.entity;
 * 
 * import jakarta.persistence.*; import lombok.*; import
 * java.time.LocalDateTime;
 * 
 * @Entity
 * 
 * @Table(name = "legal_documents")
 * 
 * @Data
 * 
 * @NoArgsConstructor
 * 
 * @AllArgsConstructor
 * 
 * @Builder
 * 
 * @ToString(exclude = "registrationRequest") public class LegalDocument {
 * 
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 * 
 * @ManyToOne(fetch = FetchType.LAZY)
 * 
 * @JoinColumn(name = "registration_id", nullable = false, foreignKey
 * = @ForeignKey(name = "fk_doc_reg")) private RegistrationRequest
 * registrationRequest;
 * 
 * @Column(name = "document_type", nullable = false, length = 50) private String
 * documentType; // TRADE_LICENSE, CERTIFICATE_INCORPORATION, etc.
 * 
 * @Column(name = "file_name", nullable = false) private String fileName;
 * 
 * @Column(name = "file_storage_path", nullable = false) private String
 * fileStoragePath; // Storage key/pointer
 * 
 * @Column(name = "file_size_bytes", nullable = false) private Long
 * fileSizeBytes;
 * 
 * @Column(name = "content_type", nullable = false, length = 100) private String
 * contentType;
 * 
 * @Column(name = "uploaded_at", nullable = false) private LocalDateTime
 * uploadedAt;
 * 
 * @PrePersist protected void onCreate() { this.uploadedAt =
 * LocalDateTime.now(); } }
 */