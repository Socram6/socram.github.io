package com.cad.archive; 

import java.io.IOException; 
import org.springframework.web.bind.annotation.*; 
import org.springframework.web.multipart.MultipartFile; 
import org.springframework.http.ResponseEntity; 

@RestController
// Replace with your actual live GitHub Pages frontend URL
@CrossOrigin(origins = "https://github.io") 
public class FileController { 

    private final ArchiveService service; 

    public FileController(ArchiveService service) { 
        this.service = service; 
    } 

    @PostMapping("/upload") 
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException { 
        service.save(file); 
        return ResponseEntity.ok("CAD file archived successfully"); 
    } 
}
