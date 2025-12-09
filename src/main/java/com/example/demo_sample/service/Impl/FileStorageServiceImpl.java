package com.example.demo_sample.service.Impl;

import com.example.demo_sample.Common.ApiResponse;
import com.example.demo_sample.Common.CommonErrorCode;
import com.example.demo_sample.domain.FileStorage;
import com.example.demo_sample.domain.dto.FileResponse;
import com.example.demo_sample.repository.FileStorageRepository;
import com.example.demo_sample.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    @Autowired
    private FileStorageRepository repository;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    public FileResponse saveFile(MultipartFile file) throws IOException {
        File folder = new File(UPLOAD_DIR);
        if (!folder.exists()) folder.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = UPLOAD_DIR + fileName;

        file.transferTo(new File(filePath));

        FileStorage saved = repository.save(new FileStorage(
                null,
                fileName,
                filePath,
                file.getContentType(),
                file.getSize(),
                1
        ));

        return new FileResponse(saved);
    }


    public Resource getFile(UUID id) throws Exception {
        FileStorage file = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path path = Paths.get(file.getFilePath());

        UrlResource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Could not read file: " + file.getFileName());
        }

        return (Resource) resource; // OK: UrlResource implements Resource
    }


    @Override
    public ResponseEntity<?> deleteFile(UUID id, Authentication authentication) {

        // 1. Kiểm tra token
        if (authentication == null) {
            return ApiResponse.error(CommonErrorCode.UNAUTHORIZED, 401);
        }

        // 2. Kiểm tra quyền (ROLE_ADMIN)
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ApiResponse.error(CommonErrorCode.FORBIDDEN, 403);
        }

        // 3. Tìm file
        FileStorage file = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        CommonErrorCode.FILE_NOT_FOUND.getMessage()
                ));

        // 4. Xóa file vật lý
        File physical = new File(file.getFilePath());
        if (physical.exists()) {
            physical.delete();
        }

        // 5. Xóa khỏi DB
        repository.delete(file);

        // 6. Trả về response giống deleteTask
        return ApiResponse.success(CommonErrorCode.DELETE_FILE_SUCCESS, null);
    }

}
