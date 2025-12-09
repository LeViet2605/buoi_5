package com.example.demo_sample.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileStorage {

    @Id
    @GeneratedValue
    private UUID id;
    private String fileName;
    private String filePath;
    private String contentType;
    private long size;

    private Integer status = 1;  // active
}
