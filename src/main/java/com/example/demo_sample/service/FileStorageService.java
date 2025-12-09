package com.example.demo_sample.service;

import com.example.demo_sample.domain.dto.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileStorageService {

    FileResponse saveFile(MultipartFile file) throws IOException;

    Resource getFile(UUID id) throws Exception;

    ResponseEntity<?> deleteFile(UUID id, Authentication authentication);
}
