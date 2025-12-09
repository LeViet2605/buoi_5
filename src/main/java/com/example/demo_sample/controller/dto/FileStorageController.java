package com.example.demo_sample.controller.dto;

import com.example.demo_sample.domain.dto.FileResponse;
import com.example.demo_sample.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    // upload 1 file
    @PostMapping("/upload")
    public FileResponse uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.saveFile(file);
    }

    // upload nhiều file
    @PostMapping("/upload-multiple")
    public List<FileResponse> uploadMultiple(@RequestParam("files") List<MultipartFile> files) {
        return files.stream()
                .map(f -> {
                    try {
                        return fileStorageService.saveFile(f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) throws Exception {

        Resource resource = fileStorageService.getFile(id);

        String contentType = "application/octet-stream"; // fallback
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return fileStorageService.deleteFile(id, authentication);
    }

}

