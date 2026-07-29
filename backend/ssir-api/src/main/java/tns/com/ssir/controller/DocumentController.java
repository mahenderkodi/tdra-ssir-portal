package tns.com.ssir.controller;

import tns.com.ssir.core.entity.LegalDocument;
import tns.com.ssir.core.repository.LegalDocumentRepository;
import tns.com.ssir.service.DocumentStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private LegalDocumentRepository documentRepository;

    @PostMapping("/upload")
    public ResponseEntity<LegalDocument> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {

        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LegalDocument uploadedDoc = storageService.uploadDocument(file, documentType);
        return new ResponseEntity<>(uploadedDoc, HttpStatus.CREATED);
    }

    // --- SECURE DOWNLOAD ENDPOINT ---
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        LegalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        InputStream stream = storageService.getDocumentStream(document.getFileStoragePath());

        // Attach content headers to instruct the browser to download the file with its original filename
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, document.getContentType());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(stream));
    }
}