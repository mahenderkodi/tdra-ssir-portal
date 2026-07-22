package ae.gov.tdra.ssir.api.service;

import ae.gov.tdra.ssir.core.entity.LegalDocument;
import ae.gov.tdra.ssir.core.entity.RegistrationRequest;
import ae.gov.tdra.ssir.core.repository.LegalDocumentRepository;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

@Service
public class DocumentStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private LegalDocumentRepository documentRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // --- SUPPORT METHOD A: Standalone Upload (Used by DocumentController) ---
    public LegalDocument uploadDocument(MultipartFile file, String documentType) {
        try {
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            // Path: drafts/{type}/{uuid}.extension
            String tempStoragePath = "drafts/" + documentType.toLowerCase() + "/" + UUID.randomUUID().toString() + fileExtension;

            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(tempStoragePath)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            LegalDocument docMetadata = LegalDocument.builder()
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .fileStoragePath(tempStoragePath)
                    .fileSizeBytes(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            return documentRepository.save(docMetadata);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO drafts: " + e.getMessage(), e);
        }
    }

    // --- SUPPORT METHOD B: Unified Transactional Upload (Used by RegistrationService) ---
    public LegalDocument uploadAndLinkDocument(MultipartFile file, String documentType, RegistrationRequest request) {
        try {
            String originalFileName = file.getOriginalFilename();
            String nameWithoutExtension = originalFileName.contains(".") 
                    ? originalFileName.substring(0, originalFileName.lastIndexOf(".")) 
                    : originalFileName;
            String fileExtension = originalFileName.contains(".") 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                    : "";

            // Naming convention: registrations/reg_{id}/{originalName_id_timestamp}.extension
            long timestamp = System.currentTimeMillis();
            String sanitizedName = nameWithoutExtension.replaceAll("[^a-zA-Z0-9-_]", "_");
            String storagePath = String.format("registrations/reg_%d/%s_%d_%d%s",
                    request.getId(),
                    sanitizedName,
                    request.getId(),
                    timestamp,
                    fileExtension
            );

            // Stream file directly to MinIO
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storagePath)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // Save file metadata linked to the parent request immediately
            LegalDocument docMetadata = LegalDocument.builder()
                    .registrationRequest(request) // Linked immediately
                    .documentType(documentType)
                    .fileName(originalFileName)
                    .fileStoragePath(storagePath)
                    .fileSizeBytes(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            return documentRepository.save(docMetadata);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and link document in MinIO: " + e.getMessage(), e);
        }
    }

    // --- SUPPORT METHOD C: Document Retrieval Stream ---
    public InputStream getDocumentStream(String storagePath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve document stream from MinIO: " + e.getMessage(), e);
        }
    }
}