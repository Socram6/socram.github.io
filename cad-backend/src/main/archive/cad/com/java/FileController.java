package com.cad.archive;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins:https://socram6.github.io}")
public class FileController {

    private final ArchiveService service;

    // Maximum file size: 50MB (adjustable via environment variable)
    @Value("${file.max-size:52428800}")
    private long maxFileSize;

    public FileController(ArchiveService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file not empty
            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("File is empty"));
            }

            // Validate file size (important for mobile users with limited bandwidth)
            if (file.getSize() > maxFileSize) {
                return ResponseEntity
                        .status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ErrorResponse(
                            String.format("File size exceeds maximum of %d bytes", maxFileSize)
                        ));
            }

            // Validate content type for CAD files
            String contentType = file.getContentType();
            if (!isValidCadFileType(contentType)) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse(
                            "Invalid file type. Supported: .dwg, .dxf, .step, .iges, .pdf, .glb, .stl, .obj"
                        ));
            }

            // Save file to Supabase
            String savedFileName = service.save(file);

            return ResponseEntity.ok(new SuccessResponse(
                "CAD file archived successfully",
                savedFileName,
                file.getSize()
            ));

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Validate that uploaded file is a supported CAD format
     */
    private boolean isValidCadFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.equalsIgnoreCase("application/vnd.ms-excel") ||      // .dwg
               contentType.equalsIgnoreCase("application/dxf") ||                // .dxf
               contentType.equalsIgnoreCase("application/step") ||               // .step
               contentType.equalsIgnoreCase("application/iges") ||               // .iges
               contentType.equalsIgnoreCase("application/pdf") ||                // .pdf
               contentType.equalsIgnoreCase("model/gltf-binary") ||              // .glb
               contentType.equalsIgnoreCase("model/gltf+json") ||                // .gltf
               contentType.equalsIgnoreCase("model/stl") ||                      // .stl
               contentType.equalsIgnoreCase("model/obj") ||                      // .obj
               contentType.equalsIgnoreCase("application/octet-stream");         // fallback for 3D formats
    }

    /**
     * Success response DTO
     */
    public static class SuccessResponse {
        public String message;
        public String fileName;
        public long fileSize;

        public SuccessResponse(String message, String fileName, long fileSize) {
            this.message = message;
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
