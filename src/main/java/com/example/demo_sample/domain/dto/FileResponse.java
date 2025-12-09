package com.example.demo_sample.domain.dto;

import com.example.demo_sample.domain.FileStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private UUID id;
    private String fileName;
    private String url;
    private long size;
    private String contentType;


    public FileResponse(FileStorage file) {
        this.id = file.getId();
        this.fileName = file.getFileName();
        this.url = "/api/files/" + file.getId();
        this.size = file.getSize();
        this.contentType = file.getContentType();
    }
}
